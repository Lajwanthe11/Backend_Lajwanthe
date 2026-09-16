package com.example.rbac.config;

import com.example.rbac.dto.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for "who is making this request". Every permission
 * check in {@link PermissionAuthorizationAspect} goes through this class - a
 * client-supplied userId/tenantId in the request body/params/headers is
 * never used for authorization decisions.
 */
@Component
public class SecurityContextUtil {

    private static final String TENANT_CLAIM = "tenantId";
    private static final String USER_ID_CLAIM = "userId";

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated principal in SecurityContext");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String userId = jwt.getClaimAsString(USER_ID_CLAIM) != null
                    ? jwt.getClaimAsString(USER_ID_CLAIM)
                    : jwt.getSubject();
            String tenantId = jwt.getClaimAsString(TENANT_CLAIM);
            return new AuthenticatedUser(userId, tenantId);
        }

        // Fallback for non-JWT auth (tests, alternate auth mechanism) - still
        // sourced from the verified Authentication, never from request input.
        return new AuthenticatedUser(authentication.getName(), null);
    }
}
