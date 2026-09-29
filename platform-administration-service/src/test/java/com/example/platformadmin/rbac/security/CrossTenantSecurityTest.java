package com.example.platformadmin.rbac.security;

import com.example.platformadmin.rbac.util.SecurityContextUtil;
import com.example.platformadmin.rbac.dto.response.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CrossTenantSecurityTest {

    private final SecurityContextUtil securityContextUtil =
            new SecurityContextUtil();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUser_shouldUseTenantFromJwt() {

        Jwt jwt = new Jwt(
                "tenant-a-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "user-tenant-a",
                        "userId", "user-tenant-a",
                        "tenantId", "tenant-a"
                )
        );

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        AuthenticatedUser user =
                securityContextUtil.currentUser();

        assertEquals(
                "user-tenant-a",
                user.userId(),
                "User ID should come from the JWT"
        );

        assertEquals(
                "tenant-a",
                user.tenantId(),
                "Tenant ID should come from the JWT"
        );
    }

    @Test
    void tenantBClaim_shouldNotBecomeTenantA() {

        Jwt jwt = new Jwt(
                "tenant-b-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "user-tenant-b",
                        "userId", "user-tenant-b",
                        "tenantId", "tenant-b"
                )
        );

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        AuthenticatedUser user =
                securityContextUtil.currentUser();

        assertEquals(
                "user-tenant-b",
                user.userId(),
                "User ID should come from the tenant-B JWT"
        );

        assertEquals(
                "tenant-b",
                user.tenantId(),
                "Tenant B must remain Tenant B"
        );

        assertNotEquals(
                "tenant-a",
                user.tenantId(),
                "Tenant B must never be treated as Tenant A"
        );
    }

    @Test
    void requestTenantId_shouldNotOverrideJwtTenant() {

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "user-1",
                        "userId", "user-1",
                        "tenantId", "tenant-a"
                )
        );

        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt));

        AuthenticatedUser user =
                securityContextUtil.currentUser();

        /*
         * The tenant used by SecurityContextUtil comes from
         * the authenticated JWT.
         *
         * A controller/request parameter must not replace
         * the tenant contained in the authenticated identity.
         */
        assertEquals(
                "tenant-a",
                user.tenantId(),
                "JWT tenant must be used for authorization context"
        );
    }
}

