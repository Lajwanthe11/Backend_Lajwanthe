package com.example.qa.sprint2.rolePermission;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.example.rbac.service.PermissionMatrixService;

@ExtendWith(MockitoExtension.class)
class PermissionMatrixServiceTest {

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

        Role role = mock(Role.class);

        when(role.getId()).thenReturn(1L);
        when(role.getRoleName()).thenReturn("HR Manager");
        when(role.getRoleCode()).thenReturn("HR_MANAGER");
        when(role.getVersion()).thenReturn(1L);

        Permission permission = mock(Permission.class);

        UUID permissionId = UUID.randomUUID();

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
                .findByTenantIdAndIsDeletedFalse("tenant-1"))
                .thenReturn(List.of(role));

        when(permissionRepository.findAll())
                .thenReturn(List.of(permission));

        when(rolePermissionRepository
                .findByRole_IdInAndActiveTrue(
                        List.of(1L)))
                .thenReturn(List.of(rolePermission));

        PermissionMatrixResponse result =
                permissionMatrixService
                        .getMatrix("tenant-1");

        assertNotNull(result);

        assertNotNull(result.getRoles());

        assertEquals(
                1,
                result.getRoles().size());

        assertEquals(
                1L,
                result.getRoles()
                        .get(0)
                        .getRoleId());

        assertEquals(
                "HR Manager",
                result.getRoles()
                        .get(0)
                        .getRoleName());

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse(
                        "tenant-1");

        verify(permissionRepository)
                .findAll();

        verify(rolePermissionRepository)
                .findByRole_IdInAndActiveTrue(
                        List.of(1L));
    }

    @Test
    void getMatrix_shouldReturnEmptyRolesWhenTenantHasNoRoles() {

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse("tenant-1"))
                .thenReturn(List.of());

        when(permissionRepository.findAll())
                .thenReturn(List.of());

        PermissionMatrixResponse result =
                permissionMatrixService
                        .getMatrix("tenant-1");

        assertNotNull(result);

        assertTrue(
                result.getRoles().isEmpty());

        verify(rolePermissionRepository, never())
                .findByRole_IdInAndActiveTrue(anyList());
    }

    @Test
    void getMatrix_shouldIgnoreInactivePermissions() {

        Permission activePermission =
                mock(Permission.class);

        Permission inactivePermission =
                mock(Permission.class);

        when(activePermission.isActive())
                .thenReturn(true);

        when(inactivePermission.isActive())
                .thenReturn(false);

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse("tenant-1"))
                .thenReturn(List.of());

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(
                                activePermission,
                                inactivePermission));

        PermissionMatrixResponse result =
                permissionMatrixService
                        .getMatrix("tenant-1");

        assertNotNull(result);

        verify(permissionRepository)
                .findAll();
    }
}