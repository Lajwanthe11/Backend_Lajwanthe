package com.example.platformadmin.superadmin.platform_settings_service.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * This class provides methods to retrieve information about the currently authenticated user,
 * such as their user ID, username, and IP address. It uses Spring Security's context to access
 * the authentication details and extracts relevant claims from the JWT token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final HttpServletRequest request;

    // Retrieves the user ID of the currently authenticated user from the JWT token.
    public String getUserId() {

        log.debug("Retrieving user ID for authenticated user");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {

            String userId = jwt.getClaimAsString("user_id");
            if (userId != null && !userId.isBlank()) {
                return userId;
            }

            String subject = jwt.getSubject();
            if (subject != null && !subject.isBlank()) {
                log.debug("JWT user_id claim is unavailable; using JWT subject as user ID");
                return subject;
            }
        }

        log.warn("Unable to retrieve user ID for authenticated user");
        log.debug("Returning default user ID: SYSTEM");

        return "SYSTEM";
    }

    // Retrieves the username of the currently authenticated user from the JWT token.
    public String getUserName() {

        log.debug("Retrieving username for authenticated user");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {

            String userName = jwt.getClaimAsString("preferred_username");
            if (userName != null) {
                return userName;
            }

            return jwt.getSubject();
        }

        log.warn("Unable to retrieve username for authenticated user");
        log.debug("Returning default username: SYSTEM");

        return "SYSTEM";
    }

    // Retrieves the IP address of the client making the request, considering possible proxy headers.
    public String getIpAddress() {

        log.debug("Retrieving IP address for authenticated user");

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        log.warn("Unable to retrieve IP address for authenticated user");
        log.debug("Returning default IP address: {}", request.getRemoteAddr());

        return request.getRemoteAddr();
    }

}