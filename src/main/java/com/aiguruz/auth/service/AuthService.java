package com.aiguruz.auth.service;

import com.aiguruz.audit.service.AuditService;
import com.aiguruz.auth.dto.*;
import com.aiguruz.common.exception.*;
import com.aiguruz.tenant.model.Tenant;
import com.aiguruz.tenant.repository.TenantRepository;
import com.aiguruz.user.dto.UserDto;
import com.aiguruz.user.model.User;
import com.aiguruz.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository    userRepo;
    private final TenantRepository  tenantRepo;
    private final JwtService        jwt;
    private final AuthenticationManager authManager;
    private final PasswordEncoder   encoder;
    private final AuditService      audit;

    // ── SIGNUP ──────────────────────────────────────────────
    public AuthResponse signup(SignupRequest req, HttpServletRequest httpReq) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new BadRequestException("Email already registered");

        // Resolve or create tenant
        String tenantId;
        if (req.getTenantId() != null && !req.getTenantId().isBlank()) {
            tenantId = tenantRepo.findById(req.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"))
                .getId();
        } else {
            String tName = req.getTenantName() != null
                ? req.getTenantName() : req.getName() + "'s Institution";
            Tenant t = tenantRepo.save(Tenant.builder()
                .name(tName).status("ACTIVE").userCount(0).build());
            tenantId = t.getId();
        }

        User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(encoder.encode(req.getPassword()))
            .roles(req.getRoles())
            .activeRole(req.getRoles().get(0))
            .avatar(initials(req.getName()))
            .tenantId(tenantId)
            .active(true)
            .emailVerified(true)   // set false + send email in prod
            .build();

        user = userRepo.save(user);
        log.info("New user registered: {} roles={}", user.getEmail(), user.getRoles());

        audit.log(user.getId(), tenantId, user.getEmail(),
            "SIGNUP", "success", ip(httpReq),
            Map.of("roles", req.getRoles()));

        return AuthResponse.builder()
            .token(jwt.generate(user))
            .user(toDto(user))
            .build();
    }

    // ── LOGIN ───────────────────────────────────────────────
    public AuthResponse login(LoginRequest req, HttpServletRequest httpReq) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {}", req.getEmail());
            audit.log(null, null, req.getEmail(),
                "LOGIN_FAILED", "danger", ip(httpReq), null);
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepo.findByEmail(req.getEmail())
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setLastLoginAt(Instant.now());
        userRepo.save(user);
        log.info("User logged in: {} activeRole={}", user.getEmail(), user.getActiveRole());

        audit.log(user.getId(), user.getTenantId(), user.getEmail(),
            "LOGIN", "success", ip(httpReq),
            Map.of("activeRole", user.getActiveRole()));

        return AuthResponse.builder()
            .token(jwt.generate(user))
            .user(toDto(user))
            .build();
    }

    // ── SWITCH ROLE ─────────────────────────────────────────
    public AuthResponse switchRole(String userId, String newRole,
                                   HttpServletRequest httpReq) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.getRoles().contains(newRole))
            throw new BadRequestException("Role '" + newRole + "' is not assigned to this user");

        String oldRole = user.getActiveRole();
        user.setActiveRole(newRole);
        userRepo.save(user);
        log.info("Role switched: {} {} → {}", user.getEmail(), oldRole, newRole);

        audit.log(userId, user.getTenantId(), user.getEmail(),
            "ROLE_SWITCH", "success", ip(httpReq),
            Map.of("from", oldRole, "to", newRole));

        return AuthResponse.builder()
            .token(jwt.generate(user))
            .user(toDto(user))
            .build();
    }

    // ── Helpers ─────────────────────────────────────────────
    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        return (parts.length >= 2
            ? "" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)
            : name.substring(0, Math.min(2, name.length()))
        ).toUpperCase();
    }

    public UserDto toDto(User u) {
        return UserDto.builder()
            .id(u.getId()).name(u.getName()).email(u.getEmail())
            .roles(u.getRoles()).activeRole(u.getActiveRole())
            .avatar(u.getAvatar()).tenantId(u.getTenantId())
            .active(u.isActive()).emailVerified(u.isEmailVerified())
            .createdAt(u.getCreatedAt()).lastLoginAt(u.getLastLoginAt())
            .build();
    }

    private String ip(HttpServletRequest r) {
        String xff = r.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : r.getRemoteAddr();
    }
}

