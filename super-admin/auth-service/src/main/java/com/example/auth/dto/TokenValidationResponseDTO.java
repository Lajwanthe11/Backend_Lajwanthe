package com.example.auth.dto;

import java.util.List;

/**
 * Response DTO for inter-service token validation (POST /auth/validate).
 * Used by base-service (and any other microservice) to introspect a JWT token
 * by calling the auth-service endpoint.
 */
public class TokenValidationResponseDTO {

    private boolean valid;
    private String username;
    private String tenantId;
    private List<String> roles;
    private String message;

    public TokenValidationResponseDTO() {
    }

    public TokenValidationResponseDTO(boolean valid, String username, String tenantId,
                                      List<String> roles, String message) {
        this.valid = valid;
        this.username = username;
        this.tenantId = tenantId;
        this.roles = roles;
        this.message = message;
    }

    /** Convenience factory — successful validation result. */
    public static TokenValidationResponseDTO valid(String username, String tenantId, List<String> roles) {
        return new TokenValidationResponseDTO(true, username, tenantId, roles, "Token is valid");
    }

    /** Convenience factory — failed validation result. */
    public static TokenValidationResponseDTO invalid(String reason) {
        return new TokenValidationResponseDTO(false, null, null, null, reason);
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
