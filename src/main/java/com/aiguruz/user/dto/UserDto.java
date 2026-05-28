package com.aiguruz.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class UserDto {
    private String       id;
    private String       name;
    private String       email;
    private List<String> roles;
    private String       activeRole;
    private String       avatar;
    private String       tenantId;
    private boolean      active;
    private boolean      emailVerified;
    private Instant      createdAt;
    private Instant      lastLoginAt;
}

