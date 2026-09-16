package com.example.platformadmin.superadmin.platform_branding_service.security;

/**
 * Provides information about the currently authenticated user.
 *
 * <p>The service layer depends only on this abstraction and does not
 * directly access Spring Security's SecurityContext.</p>
 */
public interface CurrentUserProvider {

    /**
     * Returns the authenticated user's identifier.
     *
     * @return current user ID
     * @throws IllegalStateException when no authenticated user is available
     */
    String getCurrentUserId();
}