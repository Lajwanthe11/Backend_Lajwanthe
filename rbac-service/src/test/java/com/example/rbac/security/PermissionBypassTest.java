package com.example.rbac.security;

import com.example.rbac.config.RequirePermission;
import com.example.rbac.service.PermissionAuthorizationService;
import com.example.rbac.exception.PermissionDeniedException;
import com.example.rbac.service.PermissionResolver;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.time.Instant;
import java.util.Map;

import com.example.rbac.controller.IntegrationTestConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(IntegrationTestConfig.class)
class PermissionBypassTest {

    @Autowired
    private PermissionAuthorizationService authorizationService;

    @MockBean
    private PermissionResolver permissionResolver;

    private void setSecurityContext(String userId) {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", userId,
                        "userId", userId,
                        "tenantId", "tenant-1"
                )
        );
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userWithoutRequiredPermission_shouldBeDenied() {
        setSecurityContext("user-readonly-1");

        when(
                permissionResolver.hasPermission(
                        "user-readonly-1",
                        "USER_CREATE"
                )
        ).thenReturn(false);

        RequirePermission annotation =
                getRequirePermission();

        PermissionDeniedException exception =
                assertThrows(
                        PermissionDeniedException.class,
                        () ->
                                authorizationService
                                        .authorize(annotation)
                );

        assertEquals(
                "USER_CREATE",
                exception.getRequestedPermission()
        );
    }

    @Test
    void userWithRequiredPermission_shouldBeAllowed() {
        setSecurityContext("user-admin-1");

        when(
                permissionResolver.hasPermission(
                        "user-admin-1",
                        "USER_CREATE"
                )
        ).thenReturn(true);

        RequirePermission annotation =
                getRequirePermission();

        assertDoesNotThrow(
                () ->
                        authorizationService
                                .authorize(annotation)
        );
    }

    @Test
    void requireAll_whenOnePermissionMissing_shouldDeny() {
        setSecurityContext("user-hr-1");

        when(
                permissionResolver.hasPermission(
                        "user-hr-1",
                        "USER_READ"
                )
        ).thenReturn(true);

        when(
                permissionResolver.hasPermission(
                        "user-hr-1",
                        "USER_UPDATE"
                )
        ).thenReturn(false);

        RequirePermission annotation =
                getRequireAllPermission();

        PermissionDeniedException exception =
                assertThrows(
                        PermissionDeniedException.class,
                        () ->
                                authorizationService
                                        .authorize(annotation)
                );

        assertEquals(
                "USER_UPDATE",
                exception.getRequestedPermission()
        );
    }

    @Test
    void requireAny_whenOnePermissionExists_shouldAllow() {
        setSecurityContext("user-readonly-1");

        when(
                permissionResolver.hasPermission(
                        "user-readonly-1",
                        "REPORT_VIEW"
                )
        ).thenReturn(true);

        when(
                permissionResolver.hasPermission(
                        "user-readonly-1",
                        "REPORT_EXPORT"
                )
        ).thenReturn(false);

        RequirePermission annotation =
                getRequireAnyPermission();

        assertDoesNotThrow(
                () ->
                        authorizationService
                                .authorize(annotation)
        );
    }

    @Test
    void emptyPermissionAnnotation_shouldFailClosed() {
        setSecurityContext("user-admin-1");

        RequirePermission annotation =
                getEmptyPermission();

        assertThrows(
                PermissionDeniedException.class,
                () ->
                        authorizationService
                                .authorize(annotation)
        );
    }

    private RequirePermission getRequirePermission() {

        try {

            return PermissionBypassTest.class
                    .getDeclaredMethod(
                            "dummyMethod",
                            String.class
                    )
                    .getAnnotation(
                            RequirePermission.class
                    );

        } catch (NoSuchMethodException e) {

            throw new AssertionError(
                    "dummyMethod not found",
                    e
            );
        }
    }

    private RequirePermission getRequireAllPermission() {

        try {

            return PermissionBypassTest.class
                    .getDeclaredMethod(
                            "requireAllDummy"
                    )
                    .getAnnotation(
                            RequirePermission.class
                    );

        } catch (NoSuchMethodException e) {

            throw new AssertionError(
                    "requireAllDummy not found",
                    e
            );
        }
    }

    private RequirePermission getRequireAnyPermission() {

        try {

            return PermissionBypassTest.class
                    .getDeclaredMethod(
                            "requireAnyDummy"
                    )
                    .getAnnotation(
                            RequirePermission.class
                    );

        } catch (NoSuchMethodException e) {

            throw new AssertionError(
                    "requireAnyDummy not found",
                    e
            );
        }
    }

    private RequirePermission getEmptyPermission() {

        try {

            return PermissionBypassTest.class
                    .getDeclaredMethod(
                            "emptyDummy"
                    )
                    .getAnnotation(
                            RequirePermission.class
                    );

        } catch (NoSuchMethodException e) {

            throw new AssertionError(
                    "emptyDummy not found",
                    e
            );
        }
    }

    @RequirePermission("USER_CREATE")
    private void dummyMethod(String value) {
    }

    @RequirePermission(
            requireAll = {
                    "USER_READ",
                    "USER_UPDATE"
            }
    )
    private void requireAllDummy() {
    }

    @RequirePermission(
            requireAny = {
                    "REPORT_VIEW",
                    "REPORT_EXPORT"
            }
    )
    private void requireAnyDummy() {
    }

    @RequirePermission
    private void emptyDummy() {
    }
}