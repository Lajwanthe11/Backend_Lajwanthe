package com.example.rbac.service;

import com.example.rbac.dto.PermissionGroupResponseDto;
import com.example.rbac.dto.PermissionResponseDto;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.PermissionGroup;
import com.example.rbac.repository.PermissionGroupRepository;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.service.PermissionGroupNotFoundException;
import com.example.rbac.service.PermissionNotFoundException;
import com.example.rbac.service.serviceImpl.PermissionGroupServiceImpl;
import com.example.rbac.service.serviceImpl.PermissionServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTesting {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionGroupRepository permissionGroupRepository;

    private PermissionServiceImpl permissionService;
    private PermissionGroupServiceImpl permissionGroupService;

    @BeforeEach
    void setUp() {
        permissionService =
                new PermissionServiceImpl(permissionRepository);

        permissionGroupService =
                new PermissionGroupServiceImpl(
                        permissionGroupRepository,
                        permissionRepository
                );
    }

    // =========================================================
    // PermissionServiceImpl
    // =========================================================

    @Test
    void shouldListPermissionsWithFilters() {

        UUID permissionId = UUID.randomUUID();

        Permission permission = mockPermission(
                permissionId,
                "USER_CREATE",
                "USER",
                "CREATE",
                "Create Users",
                "Create users",
                true,
                true,
                "USER_MGMT"
        );

        Pageable pageable = PageRequest.of(0, 20);

        Page<Permission> page =
                new PageImpl<>(
                        List.of(permission),
                        pageable,
                        1
                );

        UUID groupId = UUID.randomUUID();

        when(permissionRepository.filter(
                eq(groupId),
                eq("USER_MGMT"),
                eq(true),
                eq(pageable)
        )).thenReturn(page);

        Page<PermissionResponseDto> result =
                permissionService.listPermissions(
                        groupId,
                        "USER_MGMT",
                        true,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        PermissionResponseDto dto =
                result.getContent().get(0);

        assertEquals(permissionId, dto.getPermissionId());
        assertEquals("USER_CREATE", dto.getPermissionCode());
        assertEquals("USER", dto.getResource());
        assertEquals("CREATE", dto.getAction());
        assertEquals("Create Users", dto.getDisplayName());
        assertEquals("Create users", dto.getDescription());
        assertTrue(dto.isActive());
        assertTrue(dto.isSystem());
        assertEquals("USER_MGMT", dto.getModule());

        verify(permissionRepository).filter(
                groupId,
                "USER_MGMT",
                true,
                pageable
        );
    }

    @Test
    void shouldGetPermissionById() {

        UUID permissionId = UUID.randomUUID();

        Permission permission = mockPermission(
                permissionId,
                "USER_READ",
                "USER",
                "READ",
                "View Users",
                "View users",
                true,
                true,
                "USER_MGMT"
        );

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        PermissionResponseDto result =
                permissionService.getById(permissionId);

        assertNotNull(result);
        assertEquals(permissionId, result.getPermissionId());
        assertEquals("USER_READ", result.getPermissionCode());
        assertEquals("View Users", result.getDisplayName());

        verify(permissionRepository)
                .findById(permissionId);
    }

    @Test
    void shouldThrowWhenPermissionDoesNotExist() {

        UUID permissionId = UUID.randomUUID();

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThrows(
                PermissionNotFoundException.class,
                () -> permissionService.getById(permissionId)
        );

        verify(permissionRepository)
                .findById(permissionId);
    }

    @Test
    void shouldSearchPermissions() {

        Permission permission1 = mockPermission(
                UUID.randomUUID(),
                "USER_CREATE",
                "USER",
                "CREATE",
                "Create Users",
                "Create users",
                true,
                true,
                "USER_MGMT"
        );

        Permission permission2 = mockPermission(
                UUID.randomUUID(),
                "USER_UPDATE",
                "USER",
                "UPDATE",
                "Edit Users",
                "Edit users",
                true,
                true,
                "USER_MGMT"
        );

        when(permissionRepository.search("USER"))
                .thenReturn(
                        List.of(
                                permission1,
                                permission2
                        )
                );

        List<PermissionResponseDto> result =
                permissionService.search("USER");

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                "USER_CREATE",
                result.get(0).getPermissionCode()
        );

        assertEquals(
                "USER_UPDATE",
                result.get(1).getPermissionCode()
        );

        verify(permissionRepository)
                .search("USER");
    }

    @Test
    void shouldReturnEmptyListWhenSearchFindsNothing() {

        when(permissionRepository.search("NOT_FOUND"))
                .thenReturn(List.of());

        List<PermissionResponseDto> result =
                permissionService.search("NOT_FOUND");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(permissionRepository)
                .search("NOT_FOUND");
    }

    @Test
    void shouldGetPermissionsByModule() {

        Permission permission = mockPermission(
                UUID.randomUUID(),
                "REPORT_VIEW",
                "REPORT",
                "VIEW",
                "View Reports",
                "View reports",
                true,
                true,
                "REPORTS"
        );

        when(
                permissionRepository
                        .findByModuleIgnoreCaseAndActiveTrue("reports")
        ).thenReturn(List.of(permission));

        List<PermissionResponseDto> result =
                permissionService.getByModule("reports");

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                "REPORT_VIEW",
                result.get(0).getPermissionCode()
        );

        assertEquals(
                "REPORTS",
                result.get(0).getModule()
        );

        verify(permissionRepository)
                .findByModuleIgnoreCaseAndActiveTrue("reports");
    }

    @Test
    void shouldReturnNullGroupInformationWhenPermissionHasNoGroup() {

        UUID permissionId = UUID.randomUUID();

        Permission permission = mockPermission(
                permissionId,
                "DASHBOARD_VIEW",
                "DASHBOARD",
                "VIEW",
                "View Dashboard",
                "View dashboard",
                true,
                true,
                "DASHBOARD"
        );

        when(permission.getGroup())
                .thenReturn(null);

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        PermissionResponseDto result =
                permissionService.getById(permissionId);

        assertNotNull(result);
        assertEquals(permissionId, result.getPermissionId());

        assertNull(result.getGroupId());
        assertNull(result.getGroupName());
    }

    // =========================================================
    // PermissionGroupServiceImpl
    // =========================================================

    @Test
    void shouldListPermissionGroups() {

        PermissionGroup group1 = mockPermissionGroup(
                UUID.randomUUID(),
                "User Management Permissions",
                "USER_MGMT_PERMS",
                "USER_MGMT",
                1,
                true
        );

        PermissionGroup group2 = mockPermissionGroup(
                UUID.randomUUID(),
                "Role Management Permissions",
                "ROLE_MGMT_PERMS",
                "ROLE_MGMT",
                2,
                true
        );

        when(permissionGroupRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(
                        List.of(group1, group2)
                );

        List<PermissionGroupResponseDto> result =
                permissionGroupService.listGroups();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                "USER_MGMT_PERMS",
                result.get(0).getGroupCode()
        );

        assertEquals(
                "ROLE_MGMT_PERMS",
                result.get(1).getGroupCode()
        );

        verify(permissionGroupRepository)
                .findAllByOrderByDisplayOrderAsc();
    }

    @Test
    void shouldReturnEmptyGroupListWhenNoGroupsExist() {

        when(permissionGroupRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of());

        List<PermissionGroupResponseDto> result =
                permissionGroupService.listGroups();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetGroupWithPermissions() {

        UUID groupId = UUID.randomUUID();

        PermissionGroup group = mockPermissionGroup(
                groupId,
                "User Management Permissions",
                "USER_MGMT_PERMS",
                "USER_MGMT",
                1,
                true
        );

        Permission permission = mockPermission(
                UUID.randomUUID(),
                "USER_CREATE",
                "USER",
                "CREATE",
                "Create Users",
                "Create users",
                true,
                true,
                "USER_MGMT"
        );

        when(permissionGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(
                permissionRepository
                        .findByGroup_GroupIdAndActiveTrue(groupId)
        ).thenReturn(List.of(permission));

        PermissionGroupResponseDto result =
                permissionGroupService
                        .getGroupWithPermissions(groupId);

        assertNotNull(result);

        assertEquals(
                groupId,
                result.getGroupId()
        );

        assertEquals(
                "User Management Permissions",
                result.getGroupName()
        );

        assertEquals(
                "USER_MGMT_PERMS",
                result.getGroupCode()
        );

        assertNotNull(result.getPermissions());
        assertEquals(1, result.getPermissions().size());

        assertEquals(
                "USER_CREATE",
                result.getPermissions()
                        .get(0)
                        .getPermissionCode()
        );

        verify(permissionGroupRepository)
                .findById(groupId);

        verify(permissionRepository)
                .findByGroup_GroupIdAndActiveTrue(groupId);
    }

    @Test
    void shouldReturnGroupWithEmptyPermissions() {

        UUID groupId = UUID.randomUUID();

        PermissionGroup group = mockPermissionGroup(
                groupId,
                "Permission Management",
                "PERMISSION_MGMT",
                "PERMISSION",
                1,
                true
        );

        when(permissionGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(
                permissionRepository
                        .findByGroup_GroupIdAndActiveTrue(groupId)
        ).thenReturn(List.of());

        PermissionGroupResponseDto result =
                permissionGroupService
                        .getGroupWithPermissions(groupId);

        assertNotNull(result);
        assertNotNull(result.getPermissions());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void shouldThrowWhenPermissionGroupDoesNotExist() {

        UUID groupId = UUID.randomUUID();

        when(permissionGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                PermissionGroupNotFoundException.class,
                () ->
                        permissionGroupService
                                .getGroupWithPermissions(groupId)
        );
    }

    // =========================================================
    // Test Helpers
    // =========================================================

    private Permission mockPermission(
            UUID id,
            String code,
            String resource,
            String action,
            String displayName,
            String description,
            boolean active,
            boolean system,
            String module
    ) {

        Permission permission = mock(Permission.class);

        when(permission.getPermissionId())
                .thenReturn(id);

        when(permission.getPermissionCode())
                .thenReturn(code);

        when(permission.getResource())
                .thenReturn(resource);

        when(permission.getAction())
                .thenReturn(action);

        when(permission.getDisplayName())
                .thenReturn(displayName);

        when(permission.getDescription())
                .thenReturn(description);

        when(permission.isActive())
                .thenReturn(active);

        when(permission.isSystem())
                .thenReturn(system);

        when(permission.getModule())
                .thenReturn(module);

        return permission;
    }

    private PermissionGroup mockPermissionGroup(
            UUID id,
            String name,
            String code,
            String module,
            int displayOrder,
            boolean active
    ) {

        PermissionGroup group =
                mock(PermissionGroup.class);

        when(group.getGroupId())
                .thenReturn(id);

        when(group.getGroupName())
                .thenReturn(name);

        when(group.getGroupCode())
                .thenReturn(code);

        when(group.getModule())
                .thenReturn(module);

        when(group.getDisplayOrder())
                .thenReturn(displayOrder);

        when(group.isActive())
                .thenReturn(active);

        return group;
    }
}