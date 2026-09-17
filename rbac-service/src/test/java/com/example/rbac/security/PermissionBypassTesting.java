package com.example.rbac.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.example.rbac.config.RequirePermission;
import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import com.example.rbac.exception.PermissionDeniedException;
import com.example.rbac.service.PermissionAuthorizationService;
import com.example.rbac.service.PermissionResolver;

@SpringBootTest
class PermissionBypassTesting {

    @Autowired
    private PermissionAuthorizationService authorizationService;

    @Autowired
    private SecurityContextUtil securityContextUtil;

    @MockBean
    private PermissionResolver permissionResolver;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userWithoutRequiredPermission_shouldBeDenied() {

        setJwt(
                "user-readonly-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-readonly-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of("USER_READ")
        );

        RequirePermission annotation =
                getRequirePermission();

        PermissionDeniedException exception =
                assertThrows(
                        PermissionDeniedException.class,
                        () ->
                                authorizationService.authorize(
                                        annotation
                                )
                );

        assertEquals(
                "USER_CREATE",
                exception.getRequestedPermission()
        );
    }

    @Test
    void userWithRequiredPermission_shouldBeAllowed() {

        setJwt(
                "user-admin-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-admin-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of(
                        "USER_CREATE",
                        "USER_READ"
                )
        );

        RequirePermission annotation =
                getRequirePermission();

        assertDoesNotThrow(
                () ->
                        authorizationService.authorize(
                                annotation
                        )
        );
    }

    @Test
    void requireAll_whenOnePermissionMissing_shouldDeny() {

        setJwt(
                "user-hr-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-hr-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of("USER_READ")
        );

        RequirePermission annotation =
                getRequireAllPermission();

        PermissionDeniedException exception =
                assertThrows(
                        PermissionDeniedException.class,
                        () ->
                                authorizationService.authorize(
                                        annotation
                                )
                );

        assertEquals(
                "USER_UPDATE",
                exception.getRequestedPermission()
        );
    }

    @Test
    void requireAll_whenAllPermissionsExist_shouldAllow() {

        setJwt(
                "user-hr-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-hr-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of(
                        "USER_READ",
                        "USER_UPDATE"
                )
        );

        RequirePermission annotation =
                getRequireAllPermission();

        assertDoesNotThrow(
                () ->
                        authorizationService.authorize(
                                annotation
                        )
        );
    }

    @Test
    void requireAny_whenOnePermissionExists_shouldAllow() {

        setJwt(
                "user-readonly-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-readonly-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of("REPORT_VIEW")
        );

        RequirePermission annotation =
                getRequireAnyPermission();

        assertDoesNotThrow(
                () ->
                        authorizationService.authorize(
                                annotation
                        )
        );
    }

    @Test
    void requireAny_whenNoPermissionExists_shouldDeny() {

        setJwt(
                "user-readonly-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-readonly-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of()
        );

        RequirePermission annotation =
                getRequireAnyPermission();

        assertThrows(
                PermissionDeniedException.class,
                () ->
                        authorizationService.authorize(
                                annotation
                        )
        );
    }

    @Test
    void emptyPermissionAnnotation_shouldFailClosed() {

        setJwt(
                "user-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of()
        );

        RequirePermission annotation =
                getEmptyPermission();

        PermissionDeniedException exception =
                assertThrows(
                        PermissionDeniedException.class,
                        () ->
                                authorizationService.authorize(
                                        annotation
                                )
                );

        assertEquals(
                "UNSPECIFIED",
                exception.getRequestedPermission()
        );
    }

    @Test
    void superAdminWildcardPermission_shouldAllowAccess() {

        setJwt(
                "super-admin-1",
                "tenant-a"
        );

        /*
         * Developer implementation explicitly treats "*" as
         * the Super Admin permission.
         */
        when(
                permissionResolver.resolvePermissions(
                        "super-admin-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of("*")
        );

        RequirePermission annotation =
                getRequireAllPermission();

        assertDoesNotThrow(
                () ->
                        authorizationService.authorize(
                                annotation
                        )
        );
    }

    @Test
    void permissionsMustBeResolvedUsingJwtTenant() {

        setJwt(
                "user-1",
                "tenant-a"
        );

        when(
                permissionResolver.resolvePermissions(
                        "user-1",
                        "tenant-a"
                )
        ).thenReturn(
                Set.of("USER_READ")
        );

        RequirePermission annotation =
                getReadPermission();

        assertDoesNotThrow(
                () ->
                        authorizationService.authorize(
                                annotation
                        )
        );
    }

    private void setJwt(
            String userId,
            String tenantId
    ) {

        Jwt jwt = new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", userId,
                        "userId", userId,
                        "tenantId", tenantId
                )
        );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new JwtAuthenticationToken(jwt)
                );
    }

    private RequirePermission getRequirePermission() {

        try {
            return PermissionBypassTesting.class
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
            return PermissionBypassTesting.class
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
            return PermissionBypassTesting.class
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

    private RequirePermission getReadPermission() {

        try {
            return PermissionBypassTesting.class
                    .getDeclaredMethod(
                            "readDummy"
                    )
                    .getAnnotation(
                            RequirePermission.class
                    );

        } catch (NoSuchMethodException e) {

            throw new AssertionError(
                    "readDummy not found",
                    e
            );
        }
    }

    private RequirePermission getEmptyPermission() {

        try {
            return PermissionBypassTesting.class
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

    @RequirePermission("USER_READ")
    private void readDummy() {
    }

    @RequirePermission
    private void emptyDummy() {
    }
}