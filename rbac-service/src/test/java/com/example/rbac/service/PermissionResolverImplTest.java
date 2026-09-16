package com.example.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import com.example.common.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class PermissionResolverImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private PermissionResolverImpl permissionResolver;

    @BeforeEach
    void setUp() {
        permissionResolver = new PermissionResolverImpl(
                jdbcTemplate,
                redisTemplate);

        when(redisTemplate.opsForSet())
                .thenReturn(setOperations);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ---------------------------------------------------------
    // resolvePermissions() tests
    // ---------------------------------------------------------

    @Test
    void resolvePermissions_cacheHit_returnsCachedPermissions() {

        Set<String> cachedPermissions = Set.of(
                "USER_READ",
                "USER_CREATE");

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(cachedPermissions);

        Set<String> result = permissionResolver.resolvePermissions(
                "user1",
                "tenant1");

        assertEquals(cachedPermissions, result);

        verify(jdbcTemplate, never()).queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user1"),
                eq("tenant1"));
    }

    @Test
    void resolvePermissions_cacheMiss_returnsDatabasePermissions() {

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(List.of(
                        "USER_READ",
                        "USER_CREATE"));

        Set<String> result = permissionResolver.resolvePermissions(
                "user1",
                "tenant1");

        assertEquals(
                Set.of("USER_READ", "USER_CREATE"),
                result);
    }

    @Test
    void resolvePermissions_superAdmin_returnsWildcardPermission() {

        when(setOperations.members("perms:tenant1:admin"))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("admin"),
                eq("tenant1")))
                .thenReturn(1);

        Set<String> result = permissionResolver.resolvePermissions(
                "admin",
                "tenant1");

        assertEquals(Set.of("*"), result);

        verify(jdbcTemplate, never()).query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq("admin"),
                eq("tenant1"));
    }

    @Test
    void resolvePermissions_redisFailure_fallsBackToDatabase() {

        when(setOperations.members("perms:tenant1:user1"))
                .thenThrow(new RuntimeException("Redis unavailable"));

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(List.of("USER_READ"));

        Set<String> result = permissionResolver.resolvePermissions(
                "user1",
                "tenant1");

        assertEquals(Set.of("USER_READ"), result);
    }

    @Test
    void resolvePermissions_databaseFailure_throwsServiceUnavailable() {

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user1"),
                eq("tenant1")))
                .thenThrow(new RuntimeException("Database unavailable"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> permissionResolver.resolvePermissions(
                        "user1",
                        "tenant1"));

        assertTrue(exception.getStatusCode().is5xxServerError());
    }

    @Test
    void resolvePermissions_databaseResult_isStoredInRedisWithTtl() {

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(List.of("USER_READ"));

        Set<String> result = permissionResolver.resolvePermissions(
                "user1",
                "tenant1");

        assertEquals(Set.of("USER_READ"), result);

        verify(setOperations).add(
                "perms:tenant1:user1",
                "USER_READ");

        verify(redisTemplate).expire(
                eq("perms:tenant1:user1"),
                eq(Duration.ofMinutes(15)));
    }

    @Test
    void resolvePermissions_emptyDatabaseResult_returnsEmptySetAndCachesResult() {

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq("user1"),
                eq("tenant1")))
                .thenReturn(List.of());

        Set<String> result = permissionResolver.resolvePermissions(
                "user1",
                "tenant1");

        assertTrue(result.isEmpty());

        verify(setOperations).add(
                "perms:tenant1:user1",
                "__NO_PERMISSIONS__");

        verify(redisTemplate).expire(
                eq("perms:tenant1:user1"),
                eq(Duration.ofMinutes(15)));
    }

    @Test
    void resolvePermissions_emptyCacheMarker_returnsEmptySet() {

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(Set.of("__NO_PERMISSIONS__"));

        Set<String> result = permissionResolver.resolvePermissions(
                "user1",
                "tenant1");

        assertTrue(result.isEmpty());

        verify(jdbcTemplate, never()).queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user1"),
                eq("tenant1"));
    }

    // ---------------------------------------------------------
    // hasPermission() tests
    // ---------------------------------------------------------

    @Test
    void hasPermission_permissionExists_returnsTrue() {

        TenantContext.setTenantId("tenant1");

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(Set.of("USER_READ"));

        boolean result = permissionResolver.hasPermission(
                "user1",
                "USER_READ");

        assertTrue(result);
    }

    @Test
    void hasPermission_permissionDoesNotExist_returnsFalse() {

        TenantContext.setTenantId("tenant1");

        when(setOperations.members("perms:tenant1:user1"))
                .thenReturn(Set.of("USER_READ"));

        boolean result = permissionResolver.hasPermission(
                "user1",
                "USER_DELETE");

        assertTrue(!result);
    }

    @Test
    void hasPermission_superAdmin_returnsTrue() {

        TenantContext.setTenantId("tenant1");

        when(setOperations.members("perms:tenant1:admin"))
                .thenReturn(Set.of("*"));

        boolean result = permissionResolver.hasPermission(
                "admin",
                "ANY_PERMISSION");

        assertTrue(result);
    }

    @Test
    void hasPermission_usesCurrentTenant() {

        TenantContext.setTenantId("tenant2");

        when(setOperations.members("perms:tenant2:user1"))
                .thenReturn(Set.of("USER_READ"));

        boolean result = permissionResolver.hasPermission(
                "user1",
                "USER_READ");

        assertTrue(result);

        verify(setOperations).members(
                "perms:tenant2:user1");
    }
}