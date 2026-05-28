package com.aiguruz.auth.service;

import com.aiguruz.user.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")              private String secret;
    @Value("${jwt.access-expiry-hours}") private int    expiryHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generate(User user) {
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("userId",     user.getId())
            .claim("tenantId",   user.getTenantId())
            .claim("activeRole", user.getActiveRole())
            .claim("roles",      user.getRoles())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiryHours * 3_600_000L))
            .signWith(key())
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build()
            .parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token, String email) {
        try {
            Claims c = parse(token);
            return c.getSubject().equals(email) && c.getExpiration().after(new Date());
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) { return parse(token).getSubject(); }
}

