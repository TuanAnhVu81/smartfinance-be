package com.smartfinance.dto.response;

// Authentication response containing both tokens
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
