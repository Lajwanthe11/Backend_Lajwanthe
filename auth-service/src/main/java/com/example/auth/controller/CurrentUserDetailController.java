package com.example.auth.controller;

import com.example.auth.security.user.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lightweight "who am I" endpoint consumed by the static demo page (app.js)
 * after a successful login (form-based or SSO) to render the signed-in user.
 *
 * Relies on JwtAuthenticationFilter having already populated the
 * SecurityContext from the Bearer token on this request.
 */
@RestController
@RequestMapping("/oauth2")
@Tag(name = "Authentication", description = "Endpoints for registration, login, logout, token refresh, and password reset")
public class CurrentUserDetails {

    @GetMapping("/whoami")
    @Operation(summary = "Current user profile", description = "Returns the signed-in user's identity extracted from the JWT Bearer token.")
    public ResponseEntity<?> whoami(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        String roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", principal.getUsername());
        body.put("email", principal.getEmail());
        body.put("tenantId", principal.getTenantId());
        body.put("authorities", roles);
        body.put("principalType", "UserPrincipal");

        return ResponseEntity.ok(body);
    }
}