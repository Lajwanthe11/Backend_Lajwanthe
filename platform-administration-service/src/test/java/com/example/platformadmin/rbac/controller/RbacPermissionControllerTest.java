package com.example.platformadmin.rbac.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.common.security.jwt.JwtTokenValidator;
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

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    // Required by CommonJwtAuthenticationFilter from base-service
    @MockitoBean
    private JwtTokenValidator jwtTokenValidator;

    @BeforeEach
    void setUp() {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("userId", "user-001")
                .claim("tenantId", "tenant-001")
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ============================================================
    // POST /api/v1/auth/permissions/check
    // ============================================================

    @Test
    void shouldAllowPermissionWhenUserHasPermission()
            throws Exception {

        when(permissionCheckService.hasPermission(
                "user-001",
                "EMPLOYEE_VIEW"))
                .thenReturn(true);

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "permissionCode": "EMPLOYEE_VIEW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true));

        verify(permissionCheckService)
                .hasPermission(
                        "user-001",
                        "EMPLOYEE_VIEW");
    }

    @Test
    void shouldDenyPermissionWhenUserDoesNotHavePermission()
            throws Exception {

        when(permissionCheckService.hasPermission(
                "user-001",
                "EMPLOYEE_DELETE"))
                .thenReturn(false);

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "permissionCode": "EMPLOYEE_DELETE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false));

        verify(permissionCheckService)
                .hasPermission(
                        "user-001",
                        "EMPLOYEE_DELETE");
    }

    // ============================================================
    // Validation tests
    // ============================================================

    @Test
    void shouldRejectRequestWhenPermissionCodeIsMissing()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCheckService);
    }

    @Test
    void shouldRejectRequestWhenPermissionCodeIsBlank()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "permissionCode": " "
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
    void shouldReturnEmptyPermissionsWhenUserHasNoPermissions()
            throws Exception {

        when(permissionCheckService.getResolvedPermissions(
                "user-002",
                "tenant-001"))
                .thenReturn(Set.of());

        mockMvc.perform(
                get("/api/v1/users/user-002/permissions/resolved")
                        .param("tenantId", "tenant-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(permissionCheckService)
                .getResolvedPermissions(
                        "user-002",
                        "tenant-001");
    }

    @Test
    void shouldRequireTenantIdForResolvedPermissions()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-001/permissions/resolved"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCheckService);
    }

    // ============================================================
    // POST /api/v1/users/{userId}/permissions/cache/clear
    // ============================================================

    @Test
    void shouldClearUserPermissionCache()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/users/user-001/permissions/cache/clear")
                        .param("tenantId", "tenant-001"))
                .andExpect(status().isNoContent());

        verify(permissionCacheService)
                .clearUserPermissionsCache(
                        "user-001",
                        "tenant-001");
    }

    @Test
    void shouldRequireTenantIdForCacheClear()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/users/user-001/permissions/cache/clear"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(permissionCacheService);
    }

    // ============================================================
    // GET /api/v1/rbac/health
    // ============================================================

    @Test
    void shouldReturnUpWhenRedisIsAvailable()
            throws Exception {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        RedisConnection connection =
                mock(RedisConnection.class);

        when(redisTemplate.getConnectionFactory())
                .thenReturn(connectionFactory);

        when(connectionFactory.getConnection())
                .thenReturn(connection);

        when(connection.ping())
                .thenReturn("PONG");

        mockMvc.perform(
                get("/api/v1/rbac/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.redis").value("UP"));
    }

    @Test
    void shouldReturnDownWhenRedisIsUnavailable()
            throws Exception {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        when(redisTemplate.getConnectionFactory())
                .thenReturn(connectionFactory);

        when(connectionFactory.getConnection())
                .thenThrow(
                        new RuntimeException("Redis unavailable"));

        mockMvc.perform(
                get("/api/v1/rbac/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.redis").value("DOWN"));
    }
}
