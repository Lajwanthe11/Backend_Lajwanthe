package com.example.rbac.sprint2.rolePermission;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.rbac.controller.RolePermissionController;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.service.PermissionMatrixService;
import com.example.rbac.service.RolePermissionBatchService;
import com.example.rbac.service.RolePermissionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class RolePermissionControllerTest {

    private MockMvc mockMvc;

    private RolePermissionService rolePermissionService;

    private RolePermissionBatchService rolePermissionBatchService;

    private PermissionMatrixService permissionMatrixService;

    private ObjectMapper objectMapper;

    private final Long roleId = 1L;

    private final UUID permissionId =
            UUID.randomUUID();

    @BeforeEach
    void setUp() {

        rolePermissionService =
                mock(RolePermissionService.class);

        rolePermissionBatchService =
                mock(RolePermissionBatchService.class);

        permissionMatrixService =
                mock(PermissionMatrixService.class);

        RolePermissionController controller =
                new RolePermissionController(
                        rolePermissionService,
                        rolePermissionBatchService,
                        permissionMatrixService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getPermissions_shouldReturn200() throws Exception {

        when(rolePermissionService
                .getPermissionsByRole(roleId))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/roles/{roleId}/permissions",
                        roleId))
                .andExpect(status().isOk());

        verify(rolePermissionService)
                .getPermissionsByRole(roleId);
    }

    @Test
    void grantPermission_shouldReturn201() throws Exception {

        RolePermission rolePermission =
                mock(RolePermission.class);

        when(rolePermissionService.grantPermission(
                eq(roleId),
                eq(permissionId),
                eq("SYSTEM")))
                .thenReturn(rolePermission);

        String requestBody = """
                {
                    "permissionId": "%s",
                    "granted": true
                }
                """.formatted(permissionId);

        mockMvc.perform(
                post("/api/v1/roles/{roleId}/permissions",
                        roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        verify(rolePermissionService)
                .grantPermission(
                        roleId,
                        permissionId,
                        "SYSTEM");
    }

    @Test
    void grantPermissionWithGrantedFalse_shouldReturn204()
            throws Exception {

        String requestBody = """
                {
                    "permissionId": "%s",
                    "granted": false
                }
                """.formatted(permissionId);

        mockMvc.perform(
                post("/api/v1/roles/{roleId}/permissions",
                        roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        verify(rolePermissionService)
                .revokePermission(
                        roleId,
                        permissionId,
                        "SYSTEM");
    }

    @Test
    void revokePermission_shouldReturn204()
            throws Exception {

        mockMvc.perform(
                delete(
                        "/api/v1/roles/{roleId}/permissions/{permissionId}",
                        roleId,
                        permissionId))
                .andExpect(status().isNoContent());

        verify(rolePermissionService)
                .revokePermission(
                        roleId,
                        permissionId,
                        "SYSTEM");
    }

    @Test
    void updateBatch_shouldReturn200()
            throws Exception {

        BatchPermissionUpdateResponse response =
                new BatchPermissionUpdateResponse(
                        roleId,
                        2,
                        "Permissions updated successfully");

        when(rolePermissionBatchService.updatePermissions(
                eq(roleId),
                any(),
                eq("SYSTEM")))
                .thenReturn(response);

        String requestBody = """
                {
                    "permissions": [
                        {
                            "permissionId": "%s",
                            "granted": true
                        }
                    ],
                    "expectedVersion": 1
                }
                """.formatted(permissionId);

        mockMvc.perform(
                put("/api/v1/roles/{roleId}/permissions/batch",
                        roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.roleId")
                                .value(roleId))
                .andExpect(
                        jsonPath("$.updatedCount")
                                .value(2))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Permissions updated successfully"));

        verify(rolePermissionBatchService)
                .updatePermissions(
                        eq(roleId),
                        any(),
                        eq("SYSTEM"));
    }

    @Test
    void groupedPermissions_shouldReturn200()
            throws Exception {

        when(permissionMatrixService
                .getGroupedPermissions(roleId))
                .thenReturn(List.of());

        mockMvc.perform(
                get(
                        "/api/v1/roles/{roleId}/permissions/grouped",
                        roleId))
                .andExpect(status().isOk());

        verify(permissionMatrixService)
                .getGroupedPermissions(roleId);
    }

    @Test
    void invalidRoleId_shouldReturn400Or404()
            throws Exception {

        mockMvc.perform(
                get(
                        "/api/v1/roles/{roleId}/permissions",
                        "invalid"))
                .andExpect(
                        result -> {
                            int status =
                                    result.getResponse()
                                            .getStatus();

                            assert status == 400
                                    || status == 404;
                        });
    }
}