package com.example.rbac.controller;

import com.example.rbac.controller.PermissionController;
import com.example.rbac.dto.PermissionGroupResponseDto;
import com.example.rbac.dto.PermissionResponseDto;
import com.example.rbac.service.PermissionGroupService;
import com.example.rbac.service.PermissionService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private PermissionGroupService permissionGroupService;

    // =========================================================
    // GET /api/v1/permissions
    // =========================================================

    @Test
    void shouldListPermissions() throws Exception {

        UUID permissionId = UUID.randomUUID();

        PermissionResponseDto dto =
                new PermissionResponseDto(
                        permissionId,
                        "USER_CREATE",
                        "USER",
                        "CREATE",
                        null,
                        null,
                        "Create Users",
                        "Create users",
                        true,
                        true,
                        "USER_MGMT"
                );

        Page<PermissionResponseDto> page =
                new PageImpl<>(
                        List.of(dto),
                        PageRequest.of(0, 20),
                        1
                );

        when(permissionService.listPermissions(
                isNull(),
                isNull(),
                eq(true),
                any()
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/permissions")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                )
                .andExpect(
                        jsonPath("$.content[0].permissionCode")
                                .value("USER_CREATE")
                )
                .andExpect(
                        jsonPath("$.content[0].displayName")
                                .value("Create Users")
                )
                .andExpect(
                        jsonPath("$.content[0].active")
                                .value(true)
                );

        verify(permissionService)
                .listPermissions(
                        null,
                        null,
                        true,
                        any()
                );
    }

    // =========================================================
    // GET /api/v1/permissions?groupId=&module=&activeOnly=
    // =========================================================

    @Test
    void shouldListPermissionsWithFilters() throws Exception {

        UUID groupId = UUID.randomUUID();

        PermissionResponseDto dto =
                new PermissionResponseDto(
                        UUID.randomUUID(),
                        "USER_READ",
                        "USER",
                        "READ",
                        groupId,
                        "User Management Permissions",
                        "View Users",
                        "View users",
                        true,
                        true,
                        "USER_MGMT"
                );

        Page<PermissionResponseDto> page =
                new PageImpl<>(
                        List.of(dto),
                        PageRequest.of(0, 20),
                        1
                );

        when(permissionService.listPermissions(
                eq(groupId),
                eq("USER_MGMT"),
                eq(true),
                any()
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/permissions")
                                .param(
                                        "groupId",
                                        groupId.toString()
                                )
                                .param(
                                        "module",
                                        "USER_MGMT"
                                )
                                .param(
                                        "activeOnly",
                                        "true"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content[0].permissionCode"
                        ).value("USER_READ")
                );

        verify(permissionService)
                .listPermissions(
                        eq(groupId),
                        eq("USER_MGMT"),
                        eq(true),
                        any()
                );
    }

    // =========================================================
    // GET /api/v1/permissions/{permId}
    // =========================================================

    @Test
    void shouldGetPermissionById() throws Exception {

        UUID permissionId = UUID.randomUUID();

        PermissionResponseDto dto =
                new PermissionResponseDto(
                        permissionId,
                        "USER_CREATE",
                        "USER",
                        "CREATE",
                        null,
                        null,
                        "Create Users",
                        "Create users",
                        true,
                        true,
                        "USER_MGMT"
                );

        when(permissionService.getById(permissionId))
                .thenReturn(dto);

        mockMvc.perform(
                        get(
                                "/api/v1/permissions/{permId}",
                                permissionId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.permissionId")
                                .value(permissionId.toString())
                )
                .andExpect(
                        jsonPath("$.permissionCode")
                                .value("USER_CREATE")
                )
                .andExpect(
                        jsonPath("$.resource")
                                .value("USER")
                )
                .andExpect(
                        jsonPath("$.action")
                                .value("CREATE")
                );

        verify(permissionService)
                .getById(permissionId);
    }

    // =========================================================
    // GET /api/v1/permissions/groups
    // =========================================================

    @Test
    void shouldListPermissionGroups() throws Exception {

        UUID groupId = UUID.randomUUID();

        PermissionGroupResponseDto group =
                new PermissionGroupResponseDto(
                        groupId,
                        "User Management Permissions",
                        "USER_MGMT_PERMS",
                        "USER_MGMT",
                        1,
                        true
                );

        when(permissionGroupService.listGroups())
                .thenReturn(List.of(group));

        mockMvc.perform(
                        get("/api/v1/permissions/groups")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$").isArray()
                )
                .andExpect(
                        jsonPath("$[0].groupId")
                                .value(groupId.toString())
                )
                .andExpect(
                        jsonPath("$[0].groupName")
                                .value(
                                        "User Management Permissions"
                                )
                )
                .andExpect(
                        jsonPath("$[0].groupCode")
                                .value("USER_MGMT_PERMS")
                );

        verify(permissionGroupService)
                .listGroups();
    }

    // =========================================================
    // GET /api/v1/permissions/groups/{groupId}
    // =========================================================

    @Test
    void shouldGetPermissionGroupWithPermissions()
            throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        PermissionResponseDto permission =
                new PermissionResponseDto(
                        permissionId,
                        "USER_CREATE",
                        "USER",
                        "CREATE",
                        groupId,
                        "User Management Permissions",
                        "Create Users",
                        "Create users",
                        true,
                        true,
                        "USER_MGMT"
                );

        PermissionGroupResponseDto group =
                new PermissionGroupResponseDto(
                        groupId,
                        "User Management Permissions",
                        "USER_MGMT_PERMS",
                        "USER_MGMT",
                        1,
                        true
                );

        group.setPermissions(
                List.of(permission)
        );

        when(
                permissionGroupService
                        .getGroupWithPermissions(groupId)
        ).thenReturn(group);

        mockMvc.perform(
                        get(
                                "/api/v1/permissions/groups/{groupId}",
                                groupId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.groupId")
                                .value(groupId.toString())
                )
                .andExpect(
                        jsonPath("$.groupCode")
                                .value("USER_MGMT_PERMS")
                )
                .andExpect(
                        jsonPath("$.permissions").isArray()
                )
                .andExpect(
                        jsonPath(
                                "$.permissions[0].permissionCode"
                        ).value("USER_CREATE")
                );

        verify(permissionGroupService)
                .getGroupWithPermissions(groupId);
    }

    // =========================================================
    // GET /api/v1/permissions/search?query=
    // =========================================================

    @Test
    void shouldSearchPermissions() throws Exception {

        PermissionResponseDto dto =
                new PermissionResponseDto(
                        UUID.randomUUID(),
                        "USER_CREATE",
                        "USER",
                        "CREATE",
                        null,
                        null,
                        "Create Users",
                        "Create users",
                        true,
                        true,
                        "USER_MGMT"
                );

        when(permissionService.search("USER"))
                .thenReturn(List.of(dto));

        mockMvc.perform(
                        get("/api/v1/permissions/search")
                                .param("query", "USER")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$").isArray()
                )
                .andExpect(
                        jsonPath("$[0].permissionCode")
                                .value("USER_CREATE")
                );

        verify(permissionService)
                .search("USER");
    }

    // =========================================================
    // GET /api/v1/permissions/by-module/{mod}
    // =========================================================

    @Test
    void shouldGetPermissionsByModule()
            throws Exception {

        PermissionResponseDto dto =
                new PermissionResponseDto(
                        UUID.randomUUID(),
                        "REPORT_VIEW",
                        "REPORT",
                        "VIEW",
                        null,
                        null,
                        "View Reports",
                        "View reports",
                        true,
                        true,
                        "REPORTS"
                );

        when(permissionService.getByModule("REPORTS"))
                .thenReturn(List.of(dto));

        mockMvc.perform(
                        get(
                                "/api/v1/permissions/by-module/{mod}",
                                "REPORTS"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$").isArray()
                )
                .andExpect(
                        jsonPath("$[0].permissionCode")
                                .value("REPORT_VIEW")
                )
                .andExpect(
                        jsonPath("$[0].module")
                                .value("REPORTS")
                );

        verify(permissionService)
                .getByModule("REPORTS");
    }

    // =========================================================
    // Empty data states
    // =========================================================

    @Test
    void shouldReturnEmptyListWhenNoPermissionsExist()
            throws Exception {

        Page<PermissionResponseDto> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 20),
                        0
                );

        when(permissionService.listPermissions(
                isNull(),
                isNull(),
                eq(true),
                any()
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/permissions")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                )
                .andExpect(
                        jsonPath("$.content").isEmpty()
                );
    }

    @Test
    void shouldReturnEmptyGroupList()
            throws Exception {

        when(permissionGroupService.listGroups())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/permissions/groups")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$").isArray()
                )
                .andExpect(
                        jsonPath("$").isEmpty()
                );
    }

    // =========================================================
    // Invalid UUID
    // =========================================================

    @Test
    void shouldRejectInvalidPermissionUuid()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/permissions/{permId}",
                                "not-a-uuid"
                        )
                )
                .andExpect(status().isBadRequest());
    }
}