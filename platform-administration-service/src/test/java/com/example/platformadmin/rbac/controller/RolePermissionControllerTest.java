package com.example.platformadmin.rbac.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.platformadmin.rbac.dto.response.BatchPermissionUpdateResponse;
import com.example.platformadmin.rbac.entity.RolePermission;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionMatrixService;
import com.example.platformadmin.rbac.service.serviceImpl.RolePermissionBatchService;
import com.example.platformadmin.rbac.service.serviceImpl.RolePermissionService;

class RolePermissionControllerTest {

    private MockMvc mockMvc;

    private RolePermissionService rolePermissionService;

    private RolePermissionBatchService rolePermissionBatchService;

    private PermissionMatrixService permissionMatrixService;

    private Authentication authentication;

    private final UUID roleId = UUID.randomUUID();

    private final UUID permissionId = UUID.randomUUID();

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        rolePermissionService =
                mock(RolePermissionService.class);

        rolePermissionBatchService =
                mock(RolePermissionBatchService.class);

        permissionMatrixService =
                mock(PermissionMatrixService.class);

        /*
         * IMPORTANT:
         * The controller expects the current principal to be a
         * Spring Security Authentication object.
         *
         * Do not use:
         *
         * .principal(() -> userId.toString())
         *
         * because that creates only a java.security.Principal.
         */
        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn(userId.toString());

        RolePermissionController controller =
                new RolePermissionController(
                        rolePermissionService,
                        rolePermissionBatchService,
                        permissionMatrixService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    // ============================================================
    // GET ROLE PERMISSIONS
    // ============================================================

    @Test
    void getPermissions_shouldReturn200() throws Exception {

        when(rolePermissionService
                .getPermissionsByRole(roleId))
                .thenReturn(List.of());

        mockMvc.perform(
                get(
                        "/api/v1/roles/{roleId}/permissions",
                        roleId
                )
        )
        .andExpect(status().isOk());

        verify(rolePermissionService)
                .getPermissionsByRole(roleId);
    }

    // ============================================================
    // GRANT PERMISSION
    // ============================================================

    @Test
    void grantPermission_shouldReturn201() throws Exception {

        RolePermission rolePermission =
                mock(RolePermission.class);

        when(rolePermissionService.grantPermission(
                eq(roleId),
                eq(permissionId),
                any()
        )).thenReturn(rolePermission);

        String requestBody = """
                {
                    "permissionId": "%s",
                    "granted": true
                }
                """.formatted(permissionId);

        mockMvc.perform(
                post(
                        "/api/v1/roles/{roleId}/permissions",
                        roleId
                )
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isCreated());

        verify(rolePermissionService)
                .grantPermission(
                        eq(roleId),
                        eq(permissionId),
                        any()
                );
    }

    // ============================================================
    // REVOKE THROUGH POST
    // ============================================================

    @Test
    void grantPermissionWithGrantedFalse_shouldReturn204()
            throws Exception {

        doNothing().when(rolePermissionService).revokePermission(
                eq(roleId),
                eq(permissionId),
                any()
        );

        String requestBody = """
                {
                    "permissionId": "%s",
                    "granted": false
                }
                """.formatted(permissionId);

        mockMvc.perform(
                post(
                        "/api/v1/roles/{roleId}/permissions",
                        roleId
                )
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isNoContent());

        verify(rolePermissionService)
                .revokePermission(
                        eq(roleId),
                        eq(permissionId),
                        any()
                );
    }

    // ============================================================
    // DELETE PERMISSION
    // ============================================================

    @Test
    void revokePermission_shouldReturn204()
            throws Exception {

        doNothing().when(rolePermissionService).revokePermission(
                eq(roleId),
                eq(permissionId),
                any()
        );

        mockMvc.perform(
                delete(
                        "/api/v1/roles/{roleId}/permissions/{permissionId}",
                        roleId,
                        permissionId
                )
                .principal(authentication)
        )
        .andExpect(status().isNoContent());

        verify(rolePermissionService)
                .revokePermission(
                        eq(roleId),
                        eq(permissionId),
                        any()
                );
    }

    // ============================================================
    // BATCH UPDATE
    // ============================================================

    @Test
    void updateBatch_shouldReturn200()
            throws Exception {

        BatchPermissionUpdateResponse response =
                new BatchPermissionUpdateResponse(
                        roleId,
                        2,
                        "Permissions updated successfully"
                );

        when(rolePermissionBatchService.updatePermissions(
                eq(roleId),
                any(),
                any()
        )).thenReturn(response);

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
                put(
                        "/api/v1/roles/{roleId}/permissions/batch",
                        roleId
                )
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.roleId")
                        .value(roleId.toString())
        )
        .andExpect(
                jsonPath("$.updatedCount")
                        .value(2)
        )
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Permissions updated successfully"
                        )
        );

        verify(rolePermissionBatchService)
                .updatePermissions(
                        eq(roleId),
                        any(),
                        any()
                );
    }

    // ============================================================
    // GROUPED PERMISSIONS
    // ============================================================

    @Test
    void groupedPermissions_shouldReturn200()
            throws Exception {

        when(permissionMatrixService
                .getGroupedPermissions(roleId))
                .thenReturn(List.of());

        mockMvc.perform(
                get(
                        "/api/v1/roles/{roleId}/permissions/grouped",
                        roleId
                )
        )
        .andExpect(status().isOk());

        verify(permissionMatrixService)
                .getGroupedPermissions(roleId);
    }

    // ============================================================
    // INVALID ROLE ID
    // ============================================================

    @Test
    void invalidRoleId_shouldReturn400Or404()
            throws Exception {

        int responseStatus =
                mockMvc.perform(
                        get(
                                "/api/v1/roles/{roleId}/permissions",
                                "invalid"
                        )
                )
                .andReturn()
                .getResponse()
                .getStatus();

        assertTrue(
                responseStatus == 400 ||
                responseStatus == 404
        );
    }
}

