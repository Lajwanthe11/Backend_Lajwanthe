package com.example.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.PermissionGroup;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class PermissionMatrixTesting {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    private PermissionMatrixService matrixService;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();

        matrixService = new PermissionMatrixService(
                roleRepository,
                permissionRepository,
                rolePermissionRepository
        );
    }

    /**
     * Verifies that the service builds a complete matrix containing
     * the tenant's roles, permission groups, permissions, and
     * role-specific grant status.
     */
    @Test
    void shouldBuildCompletePermissionMatrix() {

        UUID adminRoleId = UUID.randomUUID();
        UUID employeeRoleId = UUID.randomUUID();

        Role admin = mockRole(
                adminRoleId,
                "Super Admin",
                "SUPER_ADMIN",
                5L
        );

        Role employee = mockRole(
                employeeRoleId,
                "Employee",
                "EMPLOYEE",
                3L
        );

        PermissionGroup group = mockGroup(
                UUID.randomUUID(),
                "User Management Permissions",
                1
        );

        Permission createUser = mockPermission(
                UUID.randomUUID(),
                "USER_CREATE",
                "Create Users",
                true,
                true,
                group
        );

        Permission readUser = mockPermission(
                UUID.randomUUID(),
                "USER_READ",
                "View Users",
                true,
                true,
                group
        );

        RolePermission adminGrant =
                mockRolePermission(admin, createUser);

        RolePermission employeeGrant =
                mockRolePermission(employee, readUser);

        when(
                roleRepository.findByTenantIdAndIsDeletedFalse(tenantId)
        ).thenReturn(
                List.of(admin, employee)
        );

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(createUser, readUser)
                );

        when(
                rolePermissionRepository.findByRole_IdInAndActiveTrue(
                        List.of(adminRoleId, employeeRoleId)
                )
        ).thenReturn(
                List.of(adminGrant, employeeGrant)
        );

        PermissionMatrixResponse result =
                matrixService.getMatrix(tenantId);

        assertNotNull(result);

        // Verify the roles included in the matrix.
        assertEquals(
                2,
                result.getRoles().size()
        );

        assertEquals(
                adminRoleId,
                result.getRoles()
                        .get(0)
                        .getRoleId()
        );

        assertEquals(
                "SUPER_ADMIN",
                result.getRoles()
                        .get(0)
                        .getRoleCode()
        );

        assertEquals(
                employeeRoleId,
                result.getRoles()
                        .get(1)
                        .getRoleId()
        );

        // Verify the permission group structure.
        assertEquals(
                1,
                result.getPermissionGroups().size()
        );

        PermissionMatrixResponse.PermissionGroupRow groupRow =
                result.getPermissionGroups().get(0);

        assertEquals(
                group.getGroupId().toString(),
                groupRow.getGroupId()
        );

        assertEquals(
                "User Management Permissions",
                groupRow.getGroupName()
        );

        assertEquals(
                2,
                groupRow.getPermissions().size()
        );

        // USER_CREATE should be granted only to the admin role.
        PermissionMatrixResponse.PermissionRow createRow =
                groupRow.getPermissions().get(0);

        assertEquals(
                "USER_CREATE",
                createRow.getPermCode()
        );

        assertTrue(
                createRow.getRoleGrants()
                        .get(adminRoleId.toString())
        );

        assertFalse(
                createRow.getRoleGrants()
                        .get(employeeRoleId.toString())
        );

        // USER_READ should be granted only to the employee role.
        PermissionMatrixResponse.PermissionRow readRow =
                groupRow.getPermissions().get(1);

        assertEquals(
                "USER_READ",
                readRow.getPermCode()
        );

        assertFalse(
                readRow.getRoleGrants()
                        .get(adminRoleId.toString())
        );

        assertTrue(
                readRow.getRoleGrants()
                        .get(employeeRoleId.toString())
        );

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse(tenantId);

        verify(permissionRepository)
                .findAll();

        verify(rolePermissionRepository)
                .findByRole_IdInAndActiveTrue(
                        List.of(adminRoleId, employeeRoleId)
                );
    }

    /**
     * Verifies that inactive permissions are not exposed
     * in the permission matrix.
     */
    @Test
    void shouldExcludeInactivePermissionsFromMatrix() {

        UUID roleId = UUID.randomUUID();

        Role role = mockRole(
                roleId,
                "Employee",
                "EMPLOYEE",
                1L
        );

        PermissionGroup group = mockGroup(
                UUID.randomUUID(),
                "User Management",
                1
        );

        Permission active = mockPermission(
                UUID.randomUUID(),
                "USER_READ",
                "View Users",
                true,
                true,
                group
        );

        Permission inactive = mockPermission(
                UUID.randomUUID(),
                "USER_DELETE",
                "Delete Users",
                false,
                true,
                group
        );

        when(
                roleRepository.findByTenantIdAndIsDeletedFalse(tenantId)
        ).thenReturn(
                List.of(role)
        );

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(active, inactive)
                );

        when(
                rolePermissionRepository.findByRole_IdInAndActiveTrue(
                        List.of(roleId)
                )
        ).thenReturn(
                List.of()
        );

        PermissionMatrixResponse result =
                matrixService.getMatrix(tenantId);

        assertNotNull(result);

        assertEquals(
                1,
                result.getPermissionGroups().size()
        );

        assertEquals(
                1,
                result.getPermissionGroups()
                        .get(0)
                        .getPermissions()
                        .size()
        );

        assertEquals(
                "USER_READ",
                result.getPermissionGroups()
                        .get(0)
                        .getPermissions()
                        .get(0)
                        .getPermCode()
        );
    }

    /**
     * Verifies that permissions which are not associated with
     * a permission group are not included in grouped output.
     */
    @Test
    void shouldExcludePermissionWithoutGroup() {

        UUID roleId = UUID.randomUUID();

        Role role = mockRole(
                roleId,
                "Employee",
                "EMPLOYEE",
                1L
        );

        Permission permission = mockPermission(
                UUID.randomUUID(),
                "USER_READ",
                "View Users",
                true,
                true,
                null
        );

        when(
                roleRepository.findByTenantIdAndIsDeletedFalse(tenantId)
        ).thenReturn(
                List.of(role)
        );

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(permission)
                );

        when(
                rolePermissionRepository.findByRole_IdInAndActiveTrue(
                        List.of(roleId)
                )
        ).thenReturn(
                List.of()
        );

        PermissionMatrixResponse result =
                matrixService.getMatrix(tenantId);

        assertNotNull(result);

        assertTrue(
                result.getPermissionGroups().isEmpty()
        );
    }

    /**
     * Verifies that an empty tenant role list is handled without
     * attempting to query role-permission mappings.
     */
    @Test
    void shouldHandleEmptyRoles() {

        PermissionGroup group = mockGroup(
                UUID.randomUUID(),
                "User Management",
                1
        );

        Permission permission = mockPermission(
                UUID.randomUUID(),
                "USER_READ",
                "View Users",
                true,
                true,
                group
        );

        when(
                roleRepository.findByTenantIdAndIsDeletedFalse(tenantId)
        ).thenReturn(
                List.of()
        );

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(permission)
                );

        PermissionMatrixResponse result =
                matrixService.getMatrix(tenantId);

        assertNotNull(result);

        assertTrue(
                result.getRoles().isEmpty()
        );

        verify(
                rolePermissionRepository,
                never()
        ).findByRole_IdInAndActiveTrue(anyList());
    }

    /**
     * Verifies that permissions are ordered by their display
     * name when they belong to the same permission group.
     */
    @Test
    void shouldOrderPermissionsByGroupOrderThenDisplayName() {

        UUID roleId = UUID.randomUUID();

        Role role = mockRole(
                roleId,
                "Employee",
                "EMPLOYEE",
                1L
        );

        PermissionGroup group = mockGroup(
                UUID.randomUUID(),
                "User Management",
                1
        );

        Permission zPermission = mockPermission(
                UUID.randomUUID(),
                "USER_Z",
                "Z Permission",
                true,
                true,
                group
        );

        Permission aPermission = mockPermission(
                UUID.randomUUID(),
                "USER_A",
                "A Permission",
                true,
                true,
                group
        );

        when(
                roleRepository.findByTenantIdAndIsDeletedFalse(tenantId)
        ).thenReturn(
                List.of(role)
        );

        // Deliberately return Z before A to verify service-side sorting.
        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(zPermission, aPermission)
                );

        when(
                rolePermissionRepository.findByRole_IdInAndActiveTrue(
                        List.of(roleId)
                )
        ).thenReturn(
                List.of()
        );

        PermissionMatrixResponse result =
                matrixService.getMatrix(tenantId);

        List<PermissionMatrixResponse.PermissionRow> rows =
                result.getPermissionGroups()
                        .get(0)
                        .getPermissions();

        assertEquals(
                "USER_A",
                rows.get(0).getPermCode()
        );

        assertEquals(
                "USER_Z",
                rows.get(1).getPermCode()
        );
    }

    /**
     * Verifies grouped permissions for one role, including
     * both granted and ungranted permissions.
     */
    @Test
    void shouldGetGroupedPermissionsForRole() {

        UUID roleId = UUID.randomUUID();

        Role role = mockRole(
                roleId,
                "HR Manager",
                "HR_MANAGER",
                4L
        );

        PermissionGroup group = mockGroup(
                UUID.randomUUID(),
                "User Management",
                1
        );

        Permission granted = mockPermission(
                UUID.randomUUID(),
                "USER_READ",
                "View Users",
                true,
                true,
                group
        );

        Permission notGranted = mockPermission(
                UUID.randomUUID(),
                "USER_UPDATE",
                "Edit Users",
                true,
                true,
                group
        );

        RolePermission rolePermission =
                mockRolePermission(role, granted);

        when(roleRepository.findById(roleId))
                .thenReturn(
                        Optional.of(role)
                );

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(granted, notGranted)
                );

        when(
                rolePermissionRepository.findByRole_IdAndActiveTrue(
                        roleId
                )
        ).thenReturn(
                List.of(rolePermission)
        );

        List<PermissionMatrixResponse.PermissionGroupRow> result =
                matrixService.getGroupedPermissions(roleId);

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "User Management",
                result.get(0).getGroupName()
        );

        assertEquals(
                2,
                result.get(0)
                        .getPermissions()
                        .size()
        );

        Map<String, Boolean> grantedMap =
                result.get(0)
                        .getPermissions()
                        .get(0)
                        .getRoleGrants();

        assertTrue(
                grantedMap.get(roleId.toString())
        );

        Map<String, Boolean> notGrantedMap =
                result.get(0)
                        .getPermissions()
                        .get(1)
                        .getRoleGrants();

        assertFalse(
                notGrantedMap.get(roleId.toString())
        );
    }

    /**
     * Verifies that the service rejects a request when the
     * requested role does not exist.
     */
    @Test
    void shouldThrowWhenRoleDoesNotExist() {

        UUID roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> matrixService.getGroupedPermissions(roleId)
        );

        verify(roleRepository)
                .findById(roleId);
    }

    // =========================================================
    // Test helpers
    // =========================================================

    private Role mockRole(
            UUID id,
            String name,
            String code,
            Long version
    ) {

        Role role = mock(Role.class);

        when(role.getId())
                .thenReturn(id);

        when(role.getRoleName())
                .thenReturn(name);

        when(role.getRoleCode())
                .thenReturn(code);

        when(role.getVersion())
                .thenReturn(version);

        return role;
    }

    private PermissionGroup mockGroup(
            UUID id,
            String name,
            int displayOrder
    ) {

        PermissionGroup group =
                mock(PermissionGroup.class);

        when(group.getGroupId())
                .thenReturn(id);

        when(group.getGroupName())
                .thenReturn(name);

        when(group.getDisplayOrder())
                .thenReturn(displayOrder);

        return group;
    }

    private Permission mockPermission(
            UUID id,
            String code,
            String displayName,
            boolean active,
            boolean system,
            PermissionGroup group
    ) {

        Permission permission =
                mock(Permission.class);

        when(permission.getPermissionId())
                .thenReturn(id);

        when(permission.getPermissionCode())
                .thenReturn(code);

        when(permission.getDisplayName())
                .thenReturn(displayName);

        when(permission.isActive())
                .thenReturn(active);

        when(permission.isSystem())
                .thenReturn(system);

        when(permission.getGroup())
                .thenReturn(group);

        return permission;
    }

    private RolePermission mockRolePermission(
            Role role,
            Permission permission
    ) {

        RolePermission rolePermission =
                mock(RolePermission.class);

        when(rolePermission.getRole())
                .thenReturn(role);

        when(rolePermission.getPermission())
                .thenReturn(permission);

        return rolePermission;
    }
}