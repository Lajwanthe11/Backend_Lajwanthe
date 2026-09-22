package com.example.platformadmin.superadmin.platform_branding_service.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementation of CurrentUserProvider that retrieves the current user ID
 * from the Spring Security context.
 */
@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {

/**
     * Retrieves the current authenticated user's ID from the Spring Security context.
     *
     * @return the current user's ID
     * @throws IllegalStateException if no authenticated user is found
     */

    @Override
    public String getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException("No authenticated user found");
        }

        return authentication.getName();
    }
}