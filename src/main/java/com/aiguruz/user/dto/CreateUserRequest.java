package com.aiguruz.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateUserRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8) private String password;
    @NotEmpty private List<String> roles;
    @NotBlank private String tenantId;
}
