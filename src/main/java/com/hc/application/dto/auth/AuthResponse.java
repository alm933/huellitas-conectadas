package com.hc.application.dto.auth;

public record AuthResponse(
        String token,
        String email,
        String role
) {
}