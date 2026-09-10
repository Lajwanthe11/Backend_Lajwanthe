package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for initiating a password reset.
 * The caller provides their username (and optionally tenantId).
 * A time-limited reset token is returned in the response.
 */
public class PasswordResetRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;

    /** Optional — defaults to the default tenant if not provided. */
    private String tenantId;

    public PasswordResetRequestDTO() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
