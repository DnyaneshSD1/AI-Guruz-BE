package com.aiguruz.user.service;

import com.aiguruz.auth.service.AuthService;
import com.aiguruz.common.exception.*;
import com.aiguruz.common.model.PageResponse;
import com.aiguruz.user.dto.*;
import com.aiguruz.user.model.User;
import com.aiguruz.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthService authService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("No user: " + email));
    }

    public UserDto me(String userId) {
        return authService.toDto(userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    public PageResponse<UserDto> list(String tenantId, int page, int size) {
        Page<UserDto> p = userRepo.findByTenantId(tenantId, PageRequest.of(page, size))
            .map(authService::toDto);
        return PageResponse.of(p);
    }

    public UserDto create(CreateUserRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new BadRequestException("Email already exists");
        User u = User.builder()
            .name(req.getName()).email(req.getEmail())
            .password(encoder.encode(req.getPassword()))
            .roles(req.getRoles()).activeRole(req.getRoles().get(0))
            .avatar(initials(req.getName()))
            .tenantId(req.getTenantId()).active(true).emailVerified(true)
            .build();
        log.info("Admin created user: {}", req.getEmail());
        return authService.toDto(userRepo.save(u));
    }

    public UserDto updateRoles(String id, List<String> roles) {
        User u = userRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setRoles(roles);
        if (!roles.contains(u.getActiveRole())) u.setActiveRole(roles.get(0));
        log.info("Roles updated for user {}: {}", id, roles);
        return authService.toDto(userRepo.save(u));
    }

    public void deactivate(String id) {
        User u = userRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setActive(false);
        userRepo.save(u);
        log.info("User deactivated: {}", id);
    }

    private String initials(String name) {
        String[] p = name.trim().split("\\s+");
        return (p.length >= 2
            ? "" + p[0].charAt(0) + p[p.length - 1].charAt(0)
            : name.substring(0, Math.min(2, name.length()))).toUpperCase();
    }
}

