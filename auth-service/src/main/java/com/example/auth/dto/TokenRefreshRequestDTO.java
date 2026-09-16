package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for JWT token refresh.
 */
public class TokenRefreshRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public TokenRefreshRequestDTO() {
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
