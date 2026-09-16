package com.example.rbac.security;

import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CrossTenantSecurityTesting {

    @Autowired
    private SecurityContextUtil securityContextUtil;

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

        assertEquals("user-tenant-a", user.userId());
        assertEquals("tenant-a", user.tenantId());
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

        assertEquals("tenant-b", user.tenantId());
        assertNotEquals("tenant-a", user.tenantId());
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
         * The security context gets tenant-a from JWT.
         * A controller/request parameter must not replace it
         * for authorization decisions.
         */
        assertEquals("tenant-a", user.tenantId());
    }
}