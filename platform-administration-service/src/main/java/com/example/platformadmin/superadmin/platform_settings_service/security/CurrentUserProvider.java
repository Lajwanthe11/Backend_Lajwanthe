package com.example.platformadmin.superadmin.platform_settings_service.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Provides information about the currently authenticated userfrom the shared
 * Spring Security context.
 *
 * The platform-administration-service uses the shared CommonJwtAuthenticationFilter from
 * base-service. That filter creates a JwtUserPrincipal and stores it in the SecurityContext.
 *
 * Therefore, this class uses Authentication#getName() instead of expecting the principal
 * to be a Spring OAuth2 Jwt object.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final HttpServletRequest request;

    /**
     * Retrieves the authenticated user's identifier.
     *
     * The shared security layer creates JwtUserPrincipal using the JWT subject as the username.
     * Authentication#getName() therefore returns the authenticated user's username/subject.
     *
     * @return authenticated user identifier, or SYSTEM when no authenticated user is available
     */
    public String getUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticated(authentication)) {
            return authentication.getName();
        }

        log.warn("Unable to retrieve authenticated user ID");
        return "SYSTEM";
    }

    /**
     * Retrieves the authenticated user's name.
     *
     * The shared JwtUserPrincipal exposes the JWT subject as its username, which is also
     * returned by Authentication#getName().
     *
     * @return authenticated username, or SYSTEM when no authenticated user is available
     */
    public String getUserName() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticated(authentication)) {
            return authentication.getName();
        }

        log.warn("Unable to retrieve authenticated username");
        return "SYSTEM";
    }

    /**
     * Retrieves the client IP address.
     *
     * X-Forwarded-For is checked first because the application may run behind an
     * API gateway or reverse proxy.
     *
     * @return client IP address
     */
    public String getIpAddress() {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {

            // The first address represents the original client
            // in a standard X-Forwarded-For chain.
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    /**
     * Checks whether the current authentication represents a real authenticated user.
     */
    private boolean isAuthenticated(Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

}