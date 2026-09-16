package com.example.rbac.service;

import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.PermissionGroup;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        private UUID tenantId;
        private UUID roleId;
        private UUID permissionId;
        private UUID groupId;

        private Role role;
        private Permission permission;
        private PermissionGroup permissionGroup;
        private RolePermission rolePermission;

        @BeforeEach
        void setUp() {
                tenantId = UUID.randomUUID();
                roleId = UUID.randomUUID();
                permissionId = UUID.randomUUID();
                groupId = UUID.randomUUID();

                role = mock(Role.class);
                permission = mock(Permission.class);
                permissionGroup = mock(PermissionGroup.class);
                rolePermission = mock(RolePermission.class);

                when(role.getId()).thenReturn(roleId);
                when(role.getRoleName()).thenReturn("Admin");
                when(role.getRoleCode()).thenReturn("ADMIN");

                when(permission.getPermissionId()).thenReturn(permissionId);
                when(permission.getPermissionCode()).thenReturn("USER_VIEW");
                when(permission.getDisplayName()).thenReturn("View Users");
                when(permission.isActive()).thenReturn(true);
                when(permission.isSystem()).thenReturn(false);
                when(permission.getGroup()).thenReturn(permissionGroup);

                when(permissionGroup.getGroupId()).thenReturn(groupId);
                when(permissionGroup.getGroupName()).thenReturn("User Management");
                when(permissionGroup.getDisplayOrder()).thenReturn(1);
        }

        @Test
        void shouldReturnPermissionMatrixSuccessfully() {
                when(roleRepository.findByTenantIdAndIsDeletedFalse(tenantId))
                                .thenReturn(List.of(role));

                when(permissionRepository.findAll())
                                .thenReturn(List.of(permission));

                when(rolePermissionRepository.findByRole_IdInAndActiveTrue(List.of(roleId)))
                                .thenReturn(List.of(rolePermission));

                when(rolePermission.getRole()).thenReturn(role);
                when(rolePermission.getPermission()).thenReturn(permission);

                PermissionMatrixResponse response = permissionMatrixService.getMatrix(tenantId);

                assertNotNull(response);

                assertEquals(1, response.getRoles().size());
                assertEquals(1, response.getPermissionGroups().size());

                PermissionMatrixResponse.RoleColumn roleColumn = response.getRoles().get(0);

                assertEquals(roleId, roleColumn.getRoleId());
                assertEquals("Admin", roleColumn.getRoleName());
                assertEquals("ADMIN", roleColumn.getRoleCode());

                PermissionMatrixResponse.PermissionGroupRow group = response.getPermissionGroups().get(0);

                assertEquals(groupId.toString(), group.getGroupId());
                assertEquals("User Management", group.getGroupName());
                assertEquals(1, group.getPermissions().size());

                PermissionMatrixResponse.PermissionRow permissionRow = group.getPermissions().get(0);

                assertEquals(permissionId.toString(), permissionRow.getPermId());
                assertEquals("USER_VIEW", permissionRow.getPermCode());
                assertEquals("View Users", permissionRow.getDisplayName());
                assertTrue(permissionRow.getRoleGrants().get(roleId.toString()));
        }

        @Test
        void shouldReturnFalseWhenPermissionIsNotGranted() {
                when(roleRepository.findByTenantIdAndIsDeletedFalse(tenantId))
                                .thenReturn(List.of(role));

                when(permissionRepository.findAll())
                                .thenReturn(List.of(permission));

                when(rolePermissionRepository.findByRole_IdInAndActiveTrue(List.of(roleId)))
                                .thenReturn(List.of());

                PermissionMatrixResponse response = permissionMatrixService.getMatrix(tenantId);

                PermissionMatrixResponse.PermissionRow permissionRow = response.getPermissionGroups()
                                .get(0)
                                .getPermissions()
                                .get(0);

                assertFalse(
                                permissionRow
                                                .getRoleGrants()
                                                .get(roleId.toString()));
        }

        @Test
        void shouldExcludeInactivePermissions() {
                Permission inactivePermission = mock(Permission.class);

                when(inactivePermission.isActive()).thenReturn(false);

                when(roleRepository.findByTenantIdAndIsDeletedFalse(tenantId))
                                .thenReturn(List.of(role));

                when(permissionRepository.findAll())
                                .thenReturn(List.of(permission, inactivePermission));

                when(rolePermissionRepository.findByRole_IdInAndActiveTrue(List.of(roleId)))
                                .thenReturn(List.of());

                PermissionMatrixResponse response = permissionMatrixService.getMatrix(tenantId);

                assertEquals(1, response.getPermissionGroups().size());
                assertEquals(
                                1,
                                response.getPermissionGroups()
                                                .get(0)
                                                .getPermissions()
                                                .size());
        }

        @Test
        void shouldReturnEmptyMatrixWhenNoRolesExist() {
                when(roleRepository.findByTenantIdAndIsDeletedFalse(tenantId))
                                .thenReturn(List.of());

                when(permissionRepository.findAll())
                                .thenReturn(List.of(permission));

                PermissionMatrixResponse response = permissionMatrixService.getMatrix(tenantId);

                assertNotNull(response);
                assertTrue(response.getRoles().isEmpty());
                assertEquals(1, response.getPermissionGroups().size());

                verify(rolePermissionRepository, never())
                                .findByRole_IdInAndActiveTrue(anyList());
        }

        @Test
        void shouldReturnEmptyMatrixWhenNoPermissionsExist() {
                when(roleRepository.findByTenantIdAndIsDeletedFalse(tenantId))
                                .thenReturn(List.of(role));

                when(permissionRepository.findAll())
                                .thenReturn(List.of());

                when(rolePermissionRepository.findByRole_IdInAndActiveTrue(List.of(roleId)))
                                .thenReturn(List.of());

                PermissionMatrixResponse response = permissionMatrixService.getMatrix(tenantId);

                assertNotNull(response);
                assertEquals(1, response.getRoles().size());
                assertTrue(response.getPermissionGroups().isEmpty());
        }

        @Test
        void shouldReturnGroupedPermissionsForRole() {
                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findAll())
                                .thenReturn(List.of(permission));

                when(rolePermissionRepository.findByRole_IdAndActiveTrue(roleId))
                                .thenReturn(List.of(rolePermission));

                when(rolePermission.getPermission())
                                .thenReturn(permission);

                List<PermissionMatrixResponse.PermissionGroupRow> result = permissionMatrixService
                                .getGroupedPermissions(roleId);

                assertNotNull(result);
                assertEquals(1, result.size());

                PermissionMatrixResponse.PermissionGroupRow group = result.get(0);

                assertEquals(groupId.toString(), group.getGroupId());
                assertEquals("User Management", group.getGroupName());
                assertEquals(1, group.getPermissions().size());

                PermissionMatrixResponse.PermissionRow row = group.getPermissions().get(0);

                assertEquals(permissionId.toString(), row.getPermId());
                assertEquals("USER_VIEW", row.getPermCode());
                assertEquals("View Users", row.getDisplayName());
                assertTrue(row.getRoleGrants().get(roleId.toString()));
        }

        @Test
        void shouldReturnFalseForPermissionNotGrantedInGroupedPermissions() {
                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findAll())
                                .thenReturn(List.of(permission));

                when(rolePermissionRepository.findByRole_IdAndActiveTrue(roleId))
                                .thenReturn(List.of());

                List<PermissionMatrixResponse.PermissionGroupRow> result = permissionMatrixService
                                .getGroupedPermissions(roleId);

                assertEquals(1, result.size());

                PermissionMatrixResponse.PermissionRow row = result.get(0)
                                .getPermissions()
                                .get(0);

                assertFalse(
                                row.getRoleGrants()
                                                .get(roleId.toString()));
        }

        @Test
        void shouldThrowExceptionWhenRoleDoesNotExist() {
                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.empty());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> permissionMatrixService
                                                .getGroupedPermissions(roleId));

                assertEquals("Role not found", exception.getMessage());

                verify(permissionRepository, never()).findAll();

                verify(rolePermissionRepository, never())
                                .findByRole_IdAndActiveTrue(any());
        }

        @Test
        void shouldIgnorePermissionsWithoutGroup() {
                Permission permissionWithoutGroup = mock(Permission.class);

                when(permissionWithoutGroup.isActive())
                                .thenReturn(true);

                when(permissionWithoutGroup.getGroup())
                                .thenReturn(null);

                when(permissionWithoutGroup.getDisplayName())
                                .thenReturn("Ungrouped Permission");

                when(roleRepository.findByTenantIdAndIsDeletedFalse(tenantId))
                                .thenReturn(List.of(role));

                when(permissionRepository.findAll())
                                .thenReturn(List.of(permissionWithoutGroup));

                when(rolePermissionRepository.findByRole_IdInAndActiveTrue(List.of(roleId)))
                                .thenReturn(List.of());

                PermissionMatrixResponse response = permissionMatrixService.getMatrix(tenantId);

                assertTrue(response.getPermissionGroups().isEmpty());
        }
}
