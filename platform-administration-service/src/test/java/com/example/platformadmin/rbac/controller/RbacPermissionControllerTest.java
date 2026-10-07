package com.example.platformadmin.rbac.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.platformadmin.rbac.service.PermissionCacheService;
import com.example.platformadmin.rbac.service.PermissionCheckService;

@WebMvcTest(RbacPermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class RbacPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionCheckService permissionCheckService;

    @MockitoBean
    private PermissionCacheService permissionCacheService;

    // ============================================================
    // POST /api/v1/auth/permissions/check
    // ============================================================

    @Test
    void shouldReturnAllowedTrueWhenPermissionExists()
            throws Exception {

        when(permissionCheckService.hasPermission(
                "user-001",
                "tenant-001",
                "EMPLOYEE_VIEW"))
                .thenReturn(true);

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user-001",
                                    "tenantId": "tenant-001",
                                    "permissionCode": "EMPLOYEE_VIEW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true));

        verify(permissionCheckService)
                .hasPermission(
                        "user-001",
                        "tenant-001",
                        "EMPLOYEE_VIEW");
    }

    @Test
    void shouldReturnAllowedFalseWhenPermissionDoesNotExist()
            throws Exception {

        when(permissionCheckService.hasPermission(
                "user-001",
                "tenant-001",
                "EMPLOYEE_DELETE"))
                .thenReturn(false);

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user-001",
                                    "tenantId": "tenant-001",
                                    "permissionCode": "EMPLOYEE_DELETE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false));

        verify(permissionCheckService)
                .hasPermission(
                        "user-001",
                        "tenant-001",
                        "EMPLOYEE_DELETE");
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    @Test
    void shouldRejectRequestWhenPermissionCodeIsBlank()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user-001",
                                    "tenantId": "tenant-001",
                                    "permissionCode": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCheckService);
    }

    @Test
    void shouldRejectRequestWhenPermissionCodeIsMissing()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user-001",
                                    "tenantId": "tenant-001"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCheckService);
    }

    @Test
    void shouldRejectRequestWhenUserIdIsNull()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": null,
                                    "tenantId": "tenant-001",
                                    "permissionCode": "EMPLOYEE_VIEW"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCheckService);
    }

    @Test
    void shouldRejectRequestWhenTenantIdIsNull()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user-001",
                                    "tenantId": null,
                                    "permissionCode": "EMPLOYEE_VIEW"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCheckService);
    }

    // ============================================================
    // GET /api/v1/users/{userId}/permissions/resolved
    // ============================================================

    @Test
    void shouldReturnResolvedPermissions()
            throws Exception {

        Set<String> permissions = Set.of(
                "USER_VIEW",
                "EMPLOYEE_VIEW",
                "EMPLOYEE_UPDATE");

        when(permissionCheckService.getResolvedPermissions(
                "user-001",
                "tenant-001"))
                .thenReturn(permissions);

        mockMvc.perform(
                get("/api/v1/users/user-001/permissions/resolved")
                        .param("tenantId", "tenant-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").value(
                        org.hamcrest.Matchers.containsInAnyOrder(
                                "USER_VIEW",
                                "EMPLOYEE_VIEW",
                                "EMPLOYEE_UPDATE")));

        verify(permissionCheckService)
                .getResolvedPermissions(
                        "user-001",
                        "tenant-001");
    }

    @Test
    void shouldReturnEmptyResolvedPermissions()
            throws Exception {

        when(permissionCheckService.getResolvedPermissions(
                "user-002",
                "tenant-001"))
                .thenReturn(Set.of());

        mockMvc.perform(
                get("/api/v1/users/user-002/permissions/resolved")
                        .param("tenantId", "tenant-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(permissionCheckService)
                .getResolvedPermissions(
                        "user-002",
                        "tenant-001");
    }

    // ============================================================
    // POST /api/v1/users/{userId}/permissions/cache/clear
    // ============================================================

    @Test
    void shouldClearUserPermissionCache()
            throws Exception {

        doNothing()
                .when(permissionCacheService)
                .clearUserPermissionsCache(
                        "user-001",
                        "tenant-001");

        mockMvc.perform(
                post("/api/v1/users/user-001/permissions/cache/clear")
                        .param("tenantId", "tenant-001"))
                .andExpect(status().isNoContent());

        verify(permissionCacheService)
                .clearUserPermissionsCache(
                        "user-001",
                        "tenant-001");
    }

    // ============================================================
    // REQUIRED TENANT ID
    // ============================================================

    @Test
    void shouldRequireTenantIdForResolvedPermissions()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-001/permissions/resolved"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCheckService);
    }

    @Test
    void shouldRequireTenantIdForCacheClear()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/users/user-001/permissions/cache/clear"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCacheService);
    }
}