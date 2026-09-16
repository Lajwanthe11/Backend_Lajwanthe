package com.example.qa.sprint2.permission;

import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.PermissionGroup;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.service.PermissionMatrixService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionMatrixTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    private PermissionMatrixService matrixService;

    @BeforeEach
    void setUp() {

        matrixService =
                new PermissionMatrixService(
                        roleRepository,
                        permissionRepository,
                        rolePermissionRepository
                );
    }

    // =========================================================
    // Full Matrix
    // =========================================================

    @Test
    void shouldBuildCompletePermissionMatrix() {

        Role admin = mockRole(
                1L,
                "Super Admin",
                "SUPER_ADMIN",
                5L
        );

        Role employee = mockRole(
                2L,
                "Employee",
                "EMPLOYEE",
                3L
        );

        PermissionGroup group =
                mockGroup(
                        UUID.randomUUID(),
                        "User Management Permissions",
                        1
                );

        Permission createUser =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_CREATE",
                        "Create Users",
                        true,
                        true,
                        group
                );

        Permission readUser =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_READ",
                        "View Users",
                        true,
                        true,
                        group
                );

        RolePermission adminGrant =
                mockRolePermission(
                        admin,
                        createUser
                );

        RolePermission employeeGrant =
                mockRolePermission(
                        employee,
                        readUser
                );

        when(
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-001"
                        )
        ).thenReturn(
                List.of(admin, employee)
        );

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(
                                createUser,
                                readUser
                        )
                );

        when(
                rolePermissionRepository
                        .findByRole_IdInAndActiveTrue(
                                List.of(1L, 2L)
                        )
        ).thenReturn(
                List.of(
                        adminGrant,
                        employeeGrant
                )
        );

        PermissionMatrixResponse result =
                matrixService.getMatrix("tenant-001");

        assertNotNull(result);

        // Roles
        assertEquals(
                2,
                result.getRoles().size()
        );

        assertEquals(
                1L,
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
                2L,
                result.getRoles()
                        .get(1)
                        .getRoleId()
        );

        // Permission groups
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

        // First permission
        PermissionMatrixResponse.PermissionRow createRow =
                groupRow.getPermissions().get(0);

        assertEquals(
                "USER_CREATE",
                createRow.getPermCode()
        );

        assertTrue(
                createRow
                        .getRoleGrants()
                        .get("1")
        );

        assertFalse(
                createRow
                        .getRoleGrants()
                        .get("2")
        );

        // Second permission
        PermissionMatrixResponse.PermissionRow readRow =
                groupRow.getPermissions().get(1);

        assertEquals(
                "USER_READ",
                readRow.getPermCode()
        );

        assertFalse(
                readRow
                        .getRoleGrants()
                        .get("1")
        );

        assertTrue(
                readRow
                        .getRoleGrants()
                        .get("2")
        );
    }

    // =========================================================
    // Inactive permissions
    // =========================================================

    @Test
    void shouldExcludeInactivePermissionsFromMatrix() {

        Role role =
                mockRole(
                        1L,
                        "Employee",
                        "EMPLOYEE",
                        1L
                );

        PermissionGroup group =
                mockGroup(
                        UUID.randomUUID(),
                        "User Management",
                        1
                );

        Permission active =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_READ",
                        "View Users",
                        true,
                        true,
                        group
                );

        Permission inactive =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_DELETE",
                        "Delete Users",
                        false,
                        true,
                        group
                );

        when(
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-001"
                        )
        ).thenReturn(List.of(role));

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(
                                active,
                                inactive
                        )
                );

        when(
                rolePermissionRepository
                        .findByRole_IdInAndActiveTrue(
                                List.of(1L)
                        )
        ).thenReturn(List.of());

        PermissionMatrixResponse result =
                matrixService.getMatrix("tenant-001");

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

    // =========================================================
    // Permission without group
    // =========================================================

    @Test
    void shouldExcludePermissionWithoutGroup() {

        Role role =
                mockRole(
                        1L,
                        "Employee",
                        "EMPLOYEE",
                        1L
                );

        Permission permission =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_READ",
                        "View Users",
                        true,
                        true,
                        null
                );

        when(
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-001"
                        )
        ).thenReturn(List.of(role));

        when(permissionRepository.findAll())
                .thenReturn(List.of(permission));

        when(
                rolePermissionRepository
                        .findByRole_IdInAndActiveTrue(
                                List.of(1L)
                        )
        ).thenReturn(List.of());

        PermissionMatrixResponse result =
                matrixService.getMatrix("tenant-001");

        assertNotNull(result);

        assertTrue(
                result.getPermissionGroups().isEmpty()
        );
    }

    // =========================================================
    // Empty roles
    // =========================================================

    @Test
    void shouldHandleEmptyRoles() {

        PermissionGroup group =
                mockGroup(
                        UUID.randomUUID(),
                        "User Management",
                        1
                );

        Permission permission =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_READ",
                        "View Users",
                        true,
                        true,
                        group
                );

        when(
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-001"
                        )
        ).thenReturn(List.of());

        when(permissionRepository.findAll())
                .thenReturn(List.of(permission));

        PermissionMatrixResponse result =
                matrixService.getMatrix("tenant-001");

        assertNotNull(result);

        assertTrue(
                result.getRoles().isEmpty()
        );

        /*
         * Service intentionally does not call the role-permission
         * repository when there are no role IDs.
         */
        verify(
                rolePermissionRepository,
                never()
        ).findByRole_IdInAndActiveTrue(anyList());
    }

    // =========================================================
    // Group and permission ordering
    // =========================================================

    @Test
    void shouldOrderPermissionsByGroupOrderThenDisplayName() {

        Role role =
                mockRole(
                        1L,
                        "Employee",
                        "EMPLOYEE",
                        1L
                );

        PermissionGroup group =
                mockGroup(
                        UUID.randomUUID(),
                        "User Management",
                        1
                );

        Permission zPermission =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_Z",
                        "Z Permission",
                        true,
                        true,
                        group
                );

        Permission aPermission =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_A",
                        "A Permission",
                        true,
                        true,
                        group
                );

        when(
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-001"
                        )
        ).thenReturn(List.of(role));

        /*
         * Deliberately return Z before A.
         * The service should sort them by displayName.
         */
        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(
                                zPermission,
                                aPermission
                        )
                );

        when(
                rolePermissionRepository
                        .findByRole_IdInAndActiveTrue(
                                List.of(1L)
                        )
        ).thenReturn(List.of());

        PermissionMatrixResponse result =
                matrixService.getMatrix("tenant-001");

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

    // =========================================================
    // Role-specific grouped permissions
    // =========================================================

    @Test
    void shouldGetGroupedPermissionsForRole() {

        Long roleId = 10L;

        Role role =
                mockRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        4L
                );

        PermissionGroup group =
                mockGroup(
                        UUID.randomUUID(),
                        "User Management",
                        1
                );

        Permission granted =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_READ",
                        "View Users",
                        true,
                        true,
                        group
                );

        Permission notGranted =
                mockPermission(
                        UUID.randomUUID(),
                        "USER_UPDATE",
                        "Edit Users",
                        true,
                        true,
                        group
                );

        RolePermission rolePermission =
                mockRolePermission(
                        role,
                        granted
                );

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findAll())
                .thenReturn(
                        List.of(
                                granted,
                                notGranted
                        )
                );

        when(
                rolePermissionRepository
                        .findByRole_IdAndActiveTrue(roleId)
        ).thenReturn(
                List.of(rolePermission)
        );

        List<PermissionMatrixResponse.PermissionGroupRow> result =
                matrixService.getGroupedPermissions(roleId);

        assertNotNull(result);
        assertEquals(1, result.size());

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

    // =========================================================
    // Role not found
    // =========================================================

    @Test
    void shouldThrowWhenRoleDoesNotExist() {

        Long roleId = 999L;

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        matrixService
                                .getGroupedPermissions(roleId)
        );

        verify(roleRepository)
                .findById(roleId);
    }

    // =========================================================
    // Test Helpers
    // =========================================================

    private Role mockRole(
            Long id,
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