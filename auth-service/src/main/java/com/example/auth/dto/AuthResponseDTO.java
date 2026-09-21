package com.example.auth.dto;

import java.util.List;

/**
 * Response DTO returned after successful login, registration, or token refresh.
 */
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String username;
    private String tenantId;
    private List<String> roles;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String accessToken, String refreshToken, String tokenType,
                           String username, String tenantId, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.username = username;
        this.tenantId = tenantId;
        this.roles = roles;
    }

    public static AuthResponseDTOBuilder builder() {
        return new AuthResponseDTOBuilder();
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public static class AuthResponseDTOBuilder {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private String username;
        private String tenantId;
        private List<String> roles;

        public AuthResponseDTOBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public AuthResponseDTOBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public AuthResponseDTOBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public AuthResponseDTOBuilder username(String username) { this.username = username; return this; }
        public AuthResponseDTOBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public AuthResponseDTOBuilder roles(List<String> roles) { this.roles = roles; return this; }

        public AuthResponseDTO build() {
            return new AuthResponseDTO(accessToken, refreshToken, tokenType, username, tenantId, roles);
        }
    }
}
