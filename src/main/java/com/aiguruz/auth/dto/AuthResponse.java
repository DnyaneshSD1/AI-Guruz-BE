package com.aiguruz.auth.dto;

import com.aiguruz.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserDto user;
}
