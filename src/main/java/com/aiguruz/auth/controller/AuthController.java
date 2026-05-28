package com.aiguruz.auth.controller;

import com.aiguruz.auth.dto.*;
import com.aiguruz.auth.service.AuthService;
import com.aiguruz.common.model.ApiResponse;
import com.aiguruz.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Signup, login, role switching")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @Valid @RequestBody SignupRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.status(201)
            .body(ApiResponse.ok(authService.signup(req, httpReq), "Account created successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req, httpReq)));
    }

    @PostMapping("/switch-role")
    @Operation(summary = "Switch active role (must be pre-assigned)")
    public ResponseEntity<ApiResponse<AuthResponse>> switchRole(
            @Valid @RequestBody SwitchRoleRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.ok(ApiResponse.ok(
            authService.switchRole(SecurityUtils.currentUserId(), req.getRole(), httpReq)));
    }
}

