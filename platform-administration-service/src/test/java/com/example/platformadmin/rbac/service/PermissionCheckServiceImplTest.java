package com.example.platformadmin.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionCheckServiceImpl;

@ExtendWith(MockitoExtension.class)
class PermissionCheckServiceImplTest {

    @Mock
    private PermissionResolver permissionResolver;

    private PermissionCheckServiceImpl permissionCheckService;

    @BeforeEach
    void setUp() {
        permissionCheckService =
                new PermissionCheckServiceImpl(permissionResolver);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    // ---------------------------------------------------------
    // Core permission check tests
    // ---------------------------------------------------------

    @Test
    void hasPermission_permissionExists_returnsTrue() {

        TenantContext.setTenantId("tenant1");

        when(permissionResolver.resolvePermissions("user1", "tenant1"))
                .thenReturn(Set.of("USER_READ", "USER_CREATE"));

        boolean result = permissionCheckService.hasPermission(
                "user1",
                "USER_READ");

        assertTrue(result);
    }

    @Test
    void hasPermission_permissionDoesNotExist_returnsFalse() {

        TenantContext.setTenantId("tenant1");

        when(permissionResolver.resolvePermissions("user1", "tenant1"))
                .thenReturn(Set.of("USER_READ"));

        boolean result = permissionCheckService.hasPermission(
                "user1",
                "USER_DELETE");

        assertFalse(result);
    }

    @Test
    void hasPermission_wildcardPermission_returnsTrue() {

        TenantContext.setTenantId("tenant1");

        when(permissionResolver.resolvePermissions("user1", "tenant1"))
                .thenReturn(Set.of("*"));

        boolean result = permissionCheckService.hasPermission(
                "user1",
                "ANY_PERMISSION");

        assertTrue(result);
    }

    @Test
    void hasPermission_permissionCodeIsCaseSensitive() {

        TenantContext.setTenantId("tenant1");

        when(permissionResolver.resolvePermissions("user1", "tenant1"))
                .thenReturn(Set.of("USER_READ"));

        boolean result = permissionCheckService.hasPermission(
                "user1",
                "user_read");

        assertFalse(result);
    }

    @Test
    void getResolvedPermissions_returnsPermissionsFromResolver() {

        Set<String> permissions = Set.of(
                "USER_READ",
                "USER_CREATE",
                "USER_UPDATE");

        when(permissionResolver.resolvePermissions("user1", "tenant1"))
                .thenReturn(permissions);

        Set<String> result =
                permissionCheckService.getResolvedPermissions(
                        "user1",
                        "tenant1");

        assertEquals(permissions, result);
    }

    // ---------------------------------------------------------
    // JWT / Security Context tests
    // ---------------------------------------------------------

    @Test
    void hasPermission_jwtAuthenticatedUser_withPermission_returnsTrue() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("user1");

        SecurityContextHolder.setContext(securityContext);

        TenantContext.setTenantId("tenant1");

        when(permissionResolver.resolvePermissions(
                "user1",
                "tenant1"))
                .thenReturn(Set.of("USER_READ"));

        boolean result =
                permissionCheckService.hasPermission("USER_READ");

        assertTrue(result);
    }

    @Test
    void hasPermission_jwtAuthenticatedUser_withoutPermission_returnsFalse() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("user1");

        SecurityContextHolder.setContext(securityContext);

        TenantContext.setTenantId("tenant1");

        when(permissionResolver.resolvePermissions(
                "user1",
                "tenant1"))
                .thenReturn(Set.of("USER_READ"));

        boolean result =
                permissionCheckService.hasPermission("USER_DELETE");

        assertFalse(result);
    }

    @Test
    void hasPermission_noAuthenticatedUser_returnsFalse() {

        SecurityContextHolder.clearContext();

        boolean result =
                permissionCheckService.hasPermission("USER_READ");

        assertFalse(result);
    }

    @Test
    void hasPermission_unauthenticatedUser_returnsFalse() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        when(authentication.isAuthenticated())
                .thenReturn(false);

        SecurityContextHolder.setContext(securityContext);

        boolean result =
                permissionCheckService.hasPermission("USER_READ");

        assertFalse(result);
    }
}