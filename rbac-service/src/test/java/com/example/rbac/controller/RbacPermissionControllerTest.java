package com.example.rbac.controller;

import com.example.rbac.service.PermissionCacheService;
import com.example.rbac.service.PermissionCheckService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RbacPermissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private PermissionCacheService permissionCacheService;

    private RbacPermissionController rbacPermissionController;

    @BeforeEach
    void setUp() {

        rbacPermissionController = new RbacPermissionController(
                permissionCheckService,
                permissionCacheService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(rbacPermissionController)
                .build();
    }

    @Test
    void checkPermission_returnsAllowed() throws Exception {

        when(permissionCheckService.hasPermission(
                "user1",
                "tenant1",
                "USER_CREATE"
        )).thenReturn(true);

        String requestBody = """
                {
                    "userId": "user1",
                    "tenantId": "tenant1",
                    "permissionCode": "USER_CREATE"
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allowed").value(true));

        verify(permissionCheckService).hasPermission(
                "user1",
                "tenant1",
                "USER_CREATE"
        );
    }

    @Test
    void checkPermission_returnsDenied() throws Exception {

        when(permissionCheckService.hasPermission(
                "user1",
                "tenant1",
                "USER_DELETE"
        )).thenReturn(false);

        String requestBody = """
                {
                    "userId": "user1",
                    "tenantId": "tenant1",
                    "permissionCode": "USER_DELETE"
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allowed").value(false));

        verify(permissionCheckService).hasPermission(
                "user1",
                "tenant1",
                "USER_DELETE"
        );
    }

    @Test
    void getResolvedPermissions_returnsPermissions() throws Exception {

        when(permissionCheckService.getResolvedPermissions(
                "user1",
                "tenant1"
        )).thenReturn(Set.of(
                "USER_CREATE",
                "USER_READ"
        ));

        mockMvc.perform(
                get("/api/v1/users/user1/permissions/resolved")
                        .param("tenantId", "tenant1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[?(@ == 'USER_CREATE')]").exists())
        .andExpect(jsonPath("$[?(@ == 'USER_READ')]").exists());

        verify(permissionCheckService).getResolvedPermissions(
                "user1",
                "tenant1"
        );
    }

    @Test
    void clearPermissionCache_returnsNoContent() throws Exception {

        mockMvc.perform(
                post("/api/v1/users/user1/permissions/cache/clear")
                        .param("tenantId", "tenant1")
        )
        .andExpect(status().isNoContent());

        verify(permissionCacheService).clearUserPermissionsCache(
                "user1",
                "tenant1"
        );
    }
}