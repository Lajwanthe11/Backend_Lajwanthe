package com.example.platformadmin.rbac.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.common.security.jwt.JwtTokenValidator;
import com.example.platformadmin.rbac.controller.RbacPermissionController;
import com.example.platformadmin.rbac.service.PermissionCacheService;
import com.example.platformadmin.rbac.service.PermissionCheckService;

@WebMvcTest(RbacPermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionCheckService permissionCheckService;

    @MockitoBean
    private PermissionCacheService permissionCacheService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private JwtTokenValidator jwtTokenValidator;

    @BeforeEach
    void setUp() {
        setJwtAuthentication(
                "part12-user-with-no-role",
                "part12-tenant-a");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setJwtAuthentication(
            String userId,
            String tenantId) {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("userId", userId)
                .claim("tenantId", tenantId)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new JwtAuthenticationToken(jwt));
    }

    @Test
    @DisplayName("TC-S2-05 - User without permissions must be denied")
    void userWithoutRoles_shouldBeDenied() throws Exception {

        when(permissionCheckService.hasPermission(
                "part12-user-with-no-role",
                "USER_READ"))
                .thenReturn(false);

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissionCode": "USER_READ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false));
    }

    @Test
    @DisplayName("TC-S2-06 - Super Admin permission must always be allowed")
    void superAdmin_shouldAlwaysBeAllowed() throws Exception {

        setJwtAuthentication(
                "super-admin-user",
                "part12-tenant-a");

        when(permissionCheckService.hasPermission(
                "super-admin-user",
                "PART12_ANY_PERMISSION"))
                .thenReturn(true);

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissionCode": "PART12_ANY_PERMISSION"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true));
    }

    @Test
    @DisplayName("TC-S2-14 - Permission cache clear must return 204")
    void permissionCacheClear_shouldReturn204() throws Exception {

        mockMvc.perform(
                post("/api/v1/users/{userId}/permissions/cache/clear",
                        "part12-user")
                        .param("tenantId", "part12-tenant-a"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("TC-S2-15 - Permission check must survive Redis failure")
    void redisUnavailable_shouldFallbackToDatabase() throws Exception {

        setJwtAuthentication(
                "part12-db-backed-user",
                "part12-tenant-a");

        when(permissionCheckService.hasPermission(
                "part12-db-backed-user",
                "USER_READ"))
                .thenReturn(false);

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissionCode": "USER_READ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").isBoolean());
    }

    @Test
    @DisplayName("Resolved permission endpoint must return a JSON array")
    void resolvedPermissions_shouldReturnArray() throws Exception {

        when(permissionCheckService.getResolvedPermissions(
                "part12-user",
                "part12-tenant-a"))
                .thenReturn(Set.of("USER_READ", "USER_WRITE"));

        mockMvc.perform(
                get("/api/v1/users/{userId}/permissions/resolved",
                        "part12-user")
                        .param("tenantId", "part12-tenant-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
