package com.aiguruz.user.controller;

import com.aiguruz.common.model.*;
import com.aiguruz.common.util.SecurityUtils;
import com.aiguruz.user.dto.*;
import com.aiguruz.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management — admin/superAdmin only")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me() {
        return ResponseEntity.ok(ApiResponse.ok(userService.me(SecurityUtils.currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
            userService.list(SecurityUtils.currentTenantId(), page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(201).body(ApiResponse.ok(userService.create(req)));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateRoles(
            @PathVariable String id, @Valid @RequestBody UpdateRoleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateRoles(id, req.getRoles())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String id) {
        userService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "User deactivated"));
    }
}

