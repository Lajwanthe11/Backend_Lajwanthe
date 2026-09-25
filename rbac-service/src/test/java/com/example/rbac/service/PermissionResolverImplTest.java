package com.example.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import com.example.common.tenant.TenantContext;
import com.example.rbac.service.serviceImpl.PermissionResolverImpl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class PermissionResolverImplTest {

    private static final String USER_ID =
            "550e8400-e29b-41d4-a716-446655440000";

    private static final String ADMIN_ID =
            "550e8400-e29b-41d4-a716-446655440001";

    private static final String TENANT_1 =
            "550e8400-e29b-41d4-a716-446655440010";

    private static final String TENANT_2 =
            "550e8400-e29b-41d4-a716-446655440011";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private SimpleMeterRegistry meterRegistry;

    private PermissionResolverImpl permissionResolver;

    @BeforeEach
    void setUp() {

        meterRegistry = new SimpleMeterRegistry();

        permissionResolver = new PermissionResolverImpl(
                jdbcTemplate,
                redisTemplate,
                meterRegistry);

        when(redisTemplate.opsForSet())
                .thenReturn(setOperations);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        meterRegistry.close();
    }

    @Test
    void resolvePermissions_cacheHit_returnsCachedPermissions() {

        Set<String> cachedPermissions = Set.of(
                "USER_READ",
                "USER_CREATE");

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(cachedPermissions);

        Set<String> result =
                permissionResolver.resolvePermissions(
                        USER_ID,
                        TENANT_1);

        assertEquals(cachedPermissions, result);

        verify(jdbcTemplate, never()).queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1)));
    }

    @Test
    void resolvePermissions_cacheMiss_returnsDatabasePermissions() {

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(List.of(
                        "USER_READ",
                        "USER_CREATE"));

        Set<String> result =
                permissionResolver.resolvePermissions(
                        USER_ID,
                        TENANT_1);

        assertEquals(
                Set.of("USER_READ", "USER_CREATE"),
                result);
    }

    @Test
    void resolvePermissions_superAdmin_returnsWildcardPermission() {

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + ADMIN_ID))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(ADMIN_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(1);

        Set<String> result =
                permissionResolver.resolvePermissions(
                        ADMIN_ID,
                        TENANT_1);

        assertEquals(Set.of("*"), result);

        verify(jdbcTemplate, never()).query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                eq(UUID.fromString(ADMIN_ID)),
                eq(UUID.fromString(TENANT_1)));
    }

    @Test
    void resolvePermissions_redisFailure_fallsBackToDatabase() {

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenThrow(
                        new RuntimeException("Redis unavailable"));

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(List.of("USER_READ"));

        Set<String> result =
                permissionResolver.resolvePermissions(
                        USER_ID,
                        TENANT_1);

        assertEquals(
                Set.of("USER_READ"),
                result);

        assertEquals(
                1.0,
                meterRegistry
                        .counter("redis.cache.failures")
                        .count());
    }

    @Test
    void resolvePermissions_databaseFailure_throwsServiceUnavailable() {

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenThrow(
                        new RuntimeException(
                                "Database unavailable"));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> permissionResolver.resolvePermissions(
                                USER_ID,
                                TENANT_1));

        assertTrue(
                exception.getStatusCode()
                        .is5xxServerError());
    }

    @Test
    void resolvePermissions_databaseResult_isStoredInRedisWithTtl() {

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(List.of("USER_READ"));

        Set<String> result =
                permissionResolver.resolvePermissions(
                        USER_ID,
                        TENANT_1);

        assertEquals(
                Set.of("USER_READ"),
                result);

        verify(setOperations).add(
                "perms:" + TENANT_1 + ":" + USER_ID,
                "USER_READ");

        verify(redisTemplate).expire(
                eq("perms:" + TENANT_1 + ":" + USER_ID),
                eq(Duration.ofMinutes(15)));
    }

    @Test
    void resolvePermissions_emptyDatabaseResult_returnsEmptySetAndCachesResult() {

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1))))
                .thenReturn(List.of());

        Set<String> result =
                permissionResolver.resolvePermissions(
                        USER_ID,
                        TENANT_1);

        assertTrue(result.isEmpty());

        verify(setOperations).add(
                "perms:" + TENANT_1 + ":" + USER_ID,
                "__NO_PERMISSIONS__");

        verify(redisTemplate).expire(
                eq("perms:" + TENANT_1 + ":" + USER_ID),
                eq(Duration.ofMinutes(15)));
    }

    @Test
    void resolvePermissions_emptyCacheMarker_returnsEmptySet() {

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(Set.of("__NO_PERMISSIONS__"));

        Set<String> result =
                permissionResolver.resolvePermissions(
                        USER_ID,
                        TENANT_1);

        assertTrue(result.isEmpty());

        verify(jdbcTemplate, never()).queryForObject(
                anyString(),
                eq(Integer.class),
                eq(UUID.fromString(USER_ID)),
                eq(UUID.fromString(TENANT_1)));
    }

    @Test
    void hasPermission_permissionExists_returnsTrue() {

        TenantContext.setTenantId(TENANT_1);

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(Set.of("USER_READ"));

        boolean result =
                permissionResolver.hasPermission(
                        USER_ID,
                        "USER_READ");

        assertTrue(result);
    }

    @Test
    void hasPermission_permissionDoesNotExist_returnsFalse() {

        TenantContext.setTenantId(TENANT_1);

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + USER_ID))
                .thenReturn(Set.of("USER_READ"));

        boolean result =
                permissionResolver.hasPermission(
                        USER_ID,
                        "USER_DELETE");

        assertFalse(result);
    }

    @Test
    void hasPermission_superAdmin_returnsTrue() {

        TenantContext.setTenantId(TENANT_1);

        when(setOperations.members(
                "perms:" + TENANT_1 + ":" + ADMIN_ID))
                .thenReturn(Set.of("*"));

        boolean result =
                permissionResolver.hasPermission(
                        ADMIN_ID,
                        "ANY_PERMISSION");

        assertTrue(result);
    }

    @Test
    void hasPermission_usesCurrentTenant() {

        TenantContext.setTenantId(TENANT_2);

        when(setOperations.members(
                "perms:" + TENANT_2 + ":" + USER_ID))
                .thenReturn(Set.of("USER_READ"));

        boolean result =
                permissionResolver.hasPermission(
                        USER_ID,
                        "USER_READ");

        assertTrue(result);

        verify(setOperations).members(
                "perms:" + TENANT_2 + ":" + USER_ID);
    }
}