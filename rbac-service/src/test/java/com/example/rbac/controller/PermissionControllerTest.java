package com.example.rbac.controller;

import com.example.rbac.dto.PermissionGroupResponseDto;
import com.example.rbac.dto.PermissionResponseDto;
import com.example.rbac.exception.PermissionGroupNotFoundException;
import com.example.rbac.exception.PermissionNotFoundException;
import com.example.rbac.service.PermissionGroupService;
import com.example.rbac.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PermissionController.class, PermissionExceptionHandler.class})
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private PermissionGroupService permissionGroupService;

    private PermissionResponseDto sampleDto(String code, UUID groupId) {
        return new PermissionResponseDto(
                UUID.randomUUID(), code, "USER", "CREATE",
                groupId, "User Management Permissions",
                "Create Users", "Allows creating users",
                true, true, "USER_MGMT");
    }

    @Test
    void listPermissions_returns200AndPagedBody() throws Exception {
        UUID groupId = UUID.randomUUID();
        when(permissionService.listPermissions(any(), anyString(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleDto("USER_CREATE", groupId))));

        mockMvc.perform(get("/api/v1/permissions").param("module", "USER_MGMT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].permissionCode").value("USER_CREATE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listPermissions_returnsEmptyContent_whenNoMatches() throws Exception {
        when(permissionService.listPermissions(any(), anyString(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/permissions").param("module", "NON_EXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getPermission_returns200_whenFound() throws Exception {
        UUID permId = UUID.randomUUID();
        when(permissionService.getById(permId)).thenReturn(sampleDto("USER_CREATE", UUID.randomUUID()));

        mockMvc.perform(get("/api/v1/permissions/{permId}", permId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCode").value("USER_CREATE"));
    }

    @Test
    void getPermission_returns404_whenNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(permissionService.getById(missingId)).thenThrow(new PermissionNotFoundException(missingId));

        mockMvc.perform(get("/api/v1/permissions/{permId}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Permission not found: " + missingId));
    }

    @Test
    void getPermission_returns400_whenIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/{permId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listGroups_returns200AndGroupsList() throws Exception {
        PermissionGroupResponseDto group = new PermissionGroupResponseDto(
                UUID.randomUUID(), "User Management Permissions", "USER_MGMT_PERMS", "USER_MGMT", 1, true);
        when(permissionGroupService.listGroups()).thenReturn(List.of(group));

        mockMvc.perform(get("/api/v1/permissions/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupCode").value("USER_MGMT_PERMS"));
    }

    @Test
    void getGroup_returns200WithNestedPermissions() throws Exception {
        UUID groupId = UUID.randomUUID();
        PermissionGroupResponseDto group = new PermissionGroupResponseDto(
                groupId, "User Management Permissions", "USER_MGMT_PERMS", "USER_MGMT", 1, true);
        group.setPermissions(List.of(sampleDto("USER_CREATE", groupId)));
        when(permissionGroupService.getGroupWithPermissions(groupId)).thenReturn(group);

        mockMvc.perform(get("/api/v1/permissions/groups/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[0].permissionCode").value("USER_CREATE"));
    }

    @Test
    void getGroup_returns404_whenGroupMissing() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(permissionGroupService.getGroupWithPermissions(missingId))
                .thenThrow(new PermissionGroupNotFoundException(missingId));

        mockMvc.perform(get("/api/v1/permissions/groups/{groupId}", missingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_returns200AndMatchingPermissions() throws Exception {
        when(permissionService.search(eq("user"))).thenReturn(List.of(sampleDto("USER_CREATE", UUID.randomUUID())));

        mockMvc.perform(get("/api/v1/permissions/search").param("query", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].permissionCode").value("USER_CREATE"));
    }

    @Test
    void search_returns400_whenQueryParamMissing() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByModule_returns200AndFilteredPermissions() throws Exception {
        when(permissionService.getByModule("USER_MGMT"))
                .thenReturn(List.of(sampleDto("USER_CREATE", UUID.randomUUID())));

        mockMvc.perform(get("/api/v1/permissions/by-module/{mod}", "USER_MGMT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].module").value("USER_MGMT"));
    }

    @Test
    void getByModule_returnsEmptyArray_whenModuleHasNoPermissions() throws Exception {
        when(permissionService.getByModule("UNKNOWN_MODULE")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/permissions/by-module/{mod}", "UNKNOWN_MODULE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
