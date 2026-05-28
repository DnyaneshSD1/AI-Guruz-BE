package com.aiguruz.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SwitchRoleRequest {
    @NotBlank private String role;
}
