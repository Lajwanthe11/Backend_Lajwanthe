package com.example.rbac.service;

import com.example.rbac.service.PermissionCheckService;
import com.example.rbac.service.PermissionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionCheckSeviceTesting {

    @Mock
    private PermissionResolver permissionResolver;

    private PermissionCheckService permissionCheckService;

    @BeforeEach
    void setUp() {
        permissionCheckService =
                new PermissionCheckService(permissionResolver);
    }

    @Test
    void shouldAllowUserWhenPermissionExists() {

        when(permissionResolver.resolvePermissions(
                "user-001",
                "tenant-001"))
                .thenReturn(Set.of(
                        "USER_VIEW",
                        "USER_CREATE",
                        "USER_UPDATE"
                ));

        boolean result =
                permissionCheckService.hasPermission(
                        "user-001",
                        "tenant-001",
                        "USER_VIEW"
                );

        assertTrue(result);

        verify(permissionResolver)
                .resolvePermissions(
                        "user-001",
                        "tenant-001"
                );
    }

    @Test
    void shouldDenyUserWhenPermissionDoesNotExist() {

        when(permissionResolver.resolvePermissions(
                "user-001",
                "tenant-001"))
                .thenReturn(Set.of(
                        "USER_VIEW"
                ));

        boolean result =
                permissionCheckService.hasPermission(
                        "user-001",
                        "tenant-001",
                        "USER_DELETE"
                );

        assertFalse(result);

        verify(permissionResolver)
                .resolvePermissions(
                        "user-001",
                        "tenant-001"
                );
    }

    @Test
    void shouldAllowAnyPermissionForSuperAdminWildcard() {

        when(permissionResolver.resolvePermissions(
                "admin-001",
                "tenant-001"))
                .thenReturn(Set.of("*"));

        boolean result =
                permissionCheckService.hasPermission(
                        "admin-001",
                        "tenant-001",
                        "ANY_PERMISSION"
                );

        assertTrue(result);
    }

    @Test
    void shouldDenyWhenUserHasNoPermissions() {

        when(permissionResolver.resolvePermissions(
                "user-002",
                "tenant-001"))
                .thenReturn(Set.of());

        boolean result =
                permissionCheckService.hasPermission(
                        "user-002",
                        "tenant-001",
                        "USER_VIEW"
                );

        assertFalse(result);
    }

    @Test
    void shouldReturnResolvedPermissions() {

        Set<String> expectedPermissions = Set.of(
                "USER_VIEW",
                "EMPLOYEE_VIEW",
                "EMPLOYEE_UPDATE"
        );

        when(permissionResolver.resolvePermissions(
                "user-001",
                "tenant-001"))
                .thenReturn(expectedPermissions);

        Set<String> actualPermissions =
                permissionCheckService.getResolvedPermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                expectedPermissions,
                actualPermissions
        );
    }

    @Test
    void shouldReturnEmptyPermissionsWhenResolverReturnsEmptySet() {

        when(permissionResolver.resolvePermissions(
                "user-003",
                "tenant-001"))
                .thenReturn(Set.of());

        Set<String> result =
                permissionCheckService.getResolvedPermissions(
                        "user-003",
                        "tenant-001"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUseExactPermissionCode() {

        when(permissionResolver.resolvePermissions(
                "user-001",
                "tenant-001"))
                .thenReturn(Set.of("EMPLOYEE_VIEW"));

        assertTrue(
                permissionCheckService.hasPermission(
                        "user-001",
                        "tenant-001",
                        "EMPLOYEE_VIEW"
                )
        );

        assertFalse(
                permissionCheckService.hasPermission(
                        "user-001",
                        "tenant-001",
                        "employee_view"
                )
        );
    }
}
