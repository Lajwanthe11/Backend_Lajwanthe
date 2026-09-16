package com.example.auth.controller;

import com.example.auth.dto.TokenValidationResponseDTO;
import com.example.auth.security.jwt.JwtTokenProvider;
import com.example.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Inter-service token validation endpoint.
 *
 * Other microservices (e.g., base-service) can call POST /auth/validate
 * to perform live token introspection — useful when you need more than
 * just the JWT claims (e.g., revocation checks, user-exists checks).
 *
 * Primary auth path (per-request) still uses the shared JWT secret locally.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Token Validation", description = "Inter-service JWT token introspection endpoint")
public class TokenValidationController {

    private final JwtTokenProvider tokenProvider;

    public TokenValidationController(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Validate a JWT token and return its decoded claims.
     *
     * @param token The raw JWT string (without "Bearer " prefix)
     * @return TokenValidationResponseDTO with valid flag, username, tenantId, and roles
     */
    @PostMapping("/validate")
    @Operation(
            summary = "Validate JWT token",
            description = "Called by other microservices to introspect a JWT token. " +
                          "Returns validity flag, username, tenantId, and roles extracted from claims."
    )
    public ResponseEntity<ApiResponse<TokenValidationResponseDTO>> validateToken(
            @RequestParam("token") String token) {

        if (!StringUtils.hasText(token)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token parameter is required"));
        }

        if (!tokenProvider.validateToken(token)) {
            TokenValidationResponseDTO result = TokenValidationResponseDTO.invalid("Token is invalid or expired");
            return ResponseEntity.ok(ApiResponse.ok("Token validation failed", result));
        }

        String username = tokenProvider.getUsernameFromJWT(token);
        String tenantId = tokenProvider.getTenantIdFromJWT(token);
        String rolesRaw = tokenProvider.getRolesFromJWT(token);

        List<String> roles = (StringUtils.hasText(rolesRaw))
                ? Arrays.asList(rolesRaw.split(","))
                : List.of();

        TokenValidationResponseDTO result = TokenValidationResponseDTO.valid(username, tenantId, roles);
        return ResponseEntity.ok(ApiResponse.ok("Token is valid", result));
    }
}
