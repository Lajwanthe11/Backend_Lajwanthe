package com.example.platformadmin.rbac.util;

import com.example.common.security.user.JwtUserPrincipal;
import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.dto.response.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

// Reads the authenticated user from Spring Security’s SecurityContext. Supports OAuth2 Jwt,
// Common JwtUserPrincipal (from base-service), and active TenantContext.

@Component
public class SecurityContextUtil {

    private static final String TENANT_CLAIM = "tenantId";
    private static final String USER_ID_CLAIM = "userId";

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated principal in SecurityContext");
        }

        Object principal = authentication.getPrincipal();

        // Case 1: OAuth2 Resource Server Jwt (MockMvc tests or Spring OAuth2)
        if (principal instanceof Jwt jwt) {
            String userId = jwt.getClaimAsString(USER_ID_CLAIM) != null
                    ? jwt.getClaimAsString(USER_ID_CLAIM)
                    : jwt.getSubject();
            String tenantId = jwt.getClaimAsString(TENANT_CLAIM);
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = TenantContext.getTenantId();
            }
            return new AuthenticatedUser(userId, tenantId);
        }

        // Case 2: JwtUserPrincipal (reconstructed from JWT claims by CommonJwtAuthenticationFilter)
        if (principal instanceof JwtUserPrincipal jwtUser) {
            String userId = jwtUser.getUsername();
            String tenantId = jwtUser.getTenantId();
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = TenantContext.getTenantId();
            }
            return new AuthenticatedUser(userId, tenantId);
        }

        // Fallback for non-JWT auth (tests, alternate auth mechanism)
        if (!authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated principal in SecurityContext");
        }
        String tenantId = TenantContext.getTenantId();
        return new AuthenticatedUser(authentication.getName(), tenantId);
    }
}
