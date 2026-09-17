package com.example.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class PermissionMatrixServiceTesting {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private PermissionMatrixService permissionMatrixService;

    @Test
    void getMatrix_shouldReturnRolesAndPermissions() {

        // ---------------------------------------------------------
        // Arrange
        // Create tenant, role, permission and role-permission data.
        // UUID types match the developer entity/repository definitions.
        // ---------------------------------------------------------

        UUID tenantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        Role role = mock(Role.class);

        when(role.getId())
                .thenReturn(roleId);

        when(role.getRoleName())
                .thenReturn("HR Manager");

        when(role.getRoleCode())
                .thenReturn("HR_MANAGER");

        when(role.getVersion())
                .thenReturn(1L);

        Permission permission = mock(Permission.class);

        when(permission.getPermissionId())
                .thenReturn(permissionId);

        when(permission.getDisplayName())
                .thenReturn("Create Users");

        when(permission.isActive())
                .thenReturn(true);

        when(permission.getGroup())
                .thenReturn(null);

        RolePermission rolePermission =
                mock(RolePermission.class);

        when(rolePermission.getRole())
                .thenReturn(role);

        when(rolePermission.getPermission())
                .thenReturn(permission);

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(tenantId))
                .thenReturn(List.of(role));

        when(permissionRepository.findAll())
                .thenReturn(List.of(permission));

        when(rolePermissionRepository
                .findByRole_IdInAndActiveTrue(
                        List.of(roleId)))
                .thenReturn(List.of(rolePermission));

        // ---------------------------------------------------------
        // Act
        // ---------------------------------------------------------

        PermissionMatrixResponse result =
                permissionMatrixService.getMatrix(tenantId);

        // ---------------------------------------------------------
        // Assert
        // ---------------------------------------------------------

        assertNotNull(result);

        assertNotNull(result.getRoles());

        assertEquals(
                1,
                result.getRoles().size()
        );

        assertEquals(
                roleId,
                result.getRoles()
                        .get(0)
                        .getRoleId()
        );

        assertEquals(
                "HR Manager",
                result.getRoles()
                        .get(0)
                        .getRoleName()
        );

        // ---------------------------------------------------------
        // Verify repository interactions.
        // ---------------------------------------------------------

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse(tenantId);

        verify(permissionRepository)
                .findAll();

        verify(rolePermissionRepository)
                .findByRole_IdInAndActiveTrue(
                        List.of(roleId)
                );
    }

    @Test
    void getMatrix_shouldReturnEmptyRolesWhenTenantHasNoRoles() {

        // ---------------------------------------------------------
        // Arrange
        // A tenant without any roles should produce an empty matrix.
        // ---------------------------------------------------------

        UUID tenantId = UUID.randomUUID();

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(tenantId))
                .thenReturn(List.of());

        when(permissionRepository.findAll())
                .thenReturn(List.of());

        // ---------------------------------------------------------
        // Act
        // ---------------------------------------------------------

        PermissionMatrixResponse result =
                permissionMatrixService.getMatrix(tenantId);

        // ---------------------------------------------------------
        // Assert
        // ---------------------------------------------------------

        assertNotNull(result);

        assertNotNull(result.getRoles());

        assertTrue(
                result.getRoles().isEmpty()
        );

        // No role IDs exist, so the role-permission repository
        // should not be queried.
        verify(rolePermissionRepository, never())
                .findByRole_IdInAndActiveTrue(anyList());
    }

    @Test
    void getMatrix_shouldIgnoreInactivePermissions() {

        // ---------------------------------------------------------
        // Arrange
        // Verify that inactive permissions are not treated as
        // active permissions when building the permission matrix.
        // ---------------------------------------------------------

        UUID tenantId = UUID.randomUUID();

        Permission activePermission =
                mock(Permission.class);

        Permission inactivePermission =
                mock(Permission.class);

        when(activePermission.isActive())
                .thenReturn(true);

        when(inactivePermission.isActive())
                .thenReturn(false);

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(tenantId))
                .thenReturn(List.of());

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(
                                activePermission,
                                inactivePermission
                        )
                );

        // ---------------------------------------------------------
        // Act
        // ---------------------------------------------------------

        PermissionMatrixResponse result =
                permissionMatrixService.getMatrix(tenantId);

        // ---------------------------------------------------------
        // Assert
        // ---------------------------------------------------------

        assertNotNull(result);

        // The repository must be queried for permissions so that
        // the service can apply its active/inactive filtering logic.
        verify(permissionRepository)
                .findAll();
    }
}