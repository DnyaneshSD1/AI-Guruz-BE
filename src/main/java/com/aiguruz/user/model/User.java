package com.aiguruz.user.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Document(collection = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User implements UserDetails {
    @Id private String id;

    @Indexed(unique = true)
    private String email;

    private String password;          // BCrypt hash — never exposed in DTOs
    private String name;
    private String avatar;            // initials, e.g. "AS"
    private List<String> roles;       // ["student","teacher"]
    private String activeRole;
    private String tenantId;
    private boolean active;
    private boolean emailVerified;

    @CreatedDate      private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
    private Instant lastLoginAt;

    // UserDetails impl
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
            .collect(Collectors.toList());
    }
    @Override public String  getUsername()               { return email; }
    @Override public boolean isAccountNonExpired()       { return true; }
    @Override public boolean isAccountNonLocked()        { return active; }
    @Override public boolean isCredentialsNonExpired()   { return true; }
    @Override public boolean isEnabled()                 { return active && emailVerified; }
}

