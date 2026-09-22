package com.example.rbac.service;

import com.example.rbac.service.serviceImpl.PermissionResolverImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class PermissionResolverTest {

    private static final String EMPTY_PERMISSION_SENTINEL =
            "__NO_PERMISSIONS__";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private PermissionResolverImpl resolver;

    @BeforeEach
    void setUp() {

        resolver = new PermissionResolverImpl(
                jdbcTemplate,
                redisTemplate,
                new SimpleMeterRegistry()
        );

        lenient()
                .when(redisTemplate.opsForSet())
                .thenReturn(setOperations);
    }

    // =========================================================
    // Redis cache
    // =========================================================

    @Test
    void shouldReturnPermissionsFromRedisCache() {

        String key = "perms:tenant-001:user-001";

        Set<String> cachedPermissions = Set.of(
                "USER_READ",
                "USER_CREATE",
                "USER_UPDATE"
        );

        when(setOperations.members(key))
                .thenReturn(cachedPermissions);

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertNotNull(result);
        assertEquals(cachedPermissions, result);

        verify(setOperations).members(key);

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldNotQueryDatabaseWhenCacheHasPermissions() {

        String key = "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of("USER_READ"));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );

        verify(setOperations).members(key);

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldUseCorrectTenantAndUserInCacheKey() {

        String key = "perms:tenant-ABC:user-XYZ";

        when(setOperations.members(key))
                .thenReturn(Set.of("USER_READ"));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-XYZ",
                        "tenant-ABC"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );

        verify(setOperations).members(key);

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldKeepDifferentTenantsSeparatedInCacheKey() {

        String tenantAKey =
                "perms:tenant-A:user-001";

        String tenantBKey =
                "perms:tenant-B:user-001";

        when(setOperations.members(tenantAKey))
                .thenReturn(
                        Set.of("TENANT_A_PERMISSION")
                );

        when(setOperations.members(tenantBKey))
                .thenReturn(
                        Set.of("TENANT_B_PERMISSION")
                );

        Set<String> tenantA =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-A"
                );

        Set<String> tenantB =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-B"
                );

        assertEquals(
                Set.of("TENANT_A_PERMISSION"),
                tenantA
        );

        assertEquals(
                Set.of("TENANT_B_PERMISSION"),
                tenantB
        );

        verify(setOperations).members(tenantAKey);
        verify(setOperations).members(tenantBKey);

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldKeepDifferentUsersSeparatedInCache() {

        String userAKey =
                "perms:tenant-001:user-A";

        String userBKey =
                "perms:tenant-001:user-B";

        when(setOperations.members(userAKey))
                .thenReturn(
                        Set.of("USER_A_PERMISSION")
                );

        when(setOperations.members(userBKey))
                .thenReturn(
                        Set.of("USER_B_PERMISSION")
                );

        Set<String> userA =
                resolver.resolvePermissions(
                        "user-A",
                        "tenant-001"
                );

        Set<String> userB =
                resolver.resolvePermissions(
                        "user-B",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_A_PERMISSION"),
                userA
        );

        assertEquals(
                Set.of("USER_B_PERMISSION"),
                userB
        );

        verify(setOperations).members(userAKey);
        verify(setOperations).members(userBKey);

        verifyNoInteractions(jdbcTemplate);
    }

    // =========================================================
    // Database fallback
    // =========================================================

    @Test
    void shouldResolvePermissionsFromDatabaseWhenCacheMisses() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of(
                        "USER_READ",
                        "USER_UPDATE"
                )
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of(
                        "USER_READ",
                        "USER_UPDATE"
                ),
                result
        );

        verify(jdbcTemplate)
                .queryForObject(
                        anyString(),
                        eq(Integer.class),
                        any(),
                        any()
                );

        verify(jdbcTemplate)
                .query(
                        anyString(),
                        any(org.springframework.jdbc.core.RowMapper.class),
                        any(),
                        any()
                );
    }

    @Test
    void shouldResolveFromDatabaseWhenCacheIsEmpty() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of("USER_READ")
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );
    }

    // =========================================================
    // Super Admin
    // =========================================================

    @Test
    void shouldReturnWildcardPermissionForSuperAdmin() {

        String key =
                "perms:tenant-001:admin-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(1);

        Set<String> result =
                resolver.resolvePermissions(
                        "admin-001",
                        "tenant-001"
                );

        assertNotNull(result);

        assertEquals(
                Set.of("*"),
                result
        );

        verify(jdbcTemplate)
                .queryForObject(
                        anyString(),
                        eq(Integer.class),
                        any(),
                        any()
                );

        verify(jdbcTemplate, never())
                .query(
                        anyString(),
                        any(org.springframework.jdbc.core.RowMapper.class),
                        any(),
                        any()
                );
    }

    @Test
    void shouldCheckSuperAdminBeforeNormalPermissionResolution() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of("USER_READ")
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );

        verify(jdbcTemplate)
                .queryForObject(
                        anyString(),
                        eq(Integer.class),
                        any(),
                        any()
                );

        verify(jdbcTemplate)
                .query(
                        anyString(),
                        any(org.springframework.jdbc.core.RowMapper.class),
                        any(),
                        any()
                );
    }

    // =========================================================
    // Empty database result
    // =========================================================

    @Test
    void shouldReturnEmptySetWhenDatabaseReturnsNoPermissions() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(List.of());

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        /*
         * Developer implementation caches an empty result
         * using a sentinel value so that repeated lookups
         * do not continuously hit the database.
         */
        verify(setOperations)
                .add(
                        eq(key),
                        eq(EMPTY_PERMISSION_SENTINEL)
                );

        verify(redisTemplate)
                .expire(
                        eq(key),
                        eq(Duration.ofMinutes(15))
                );
    }

    // =========================================================
    // Redis caching after database resolution
    // =========================================================

    @Test
    void shouldStoreDatabasePermissionsInRedis() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of(
                        "USER_READ",
                        "USER_UPDATE"
                )
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of(
                        "USER_READ",
                        "USER_UPDATE"
                ),
                result
        );

        verify(setOperations)
                .add(
                        eq(key),
                        any(String[].class)
                );

        verify(redisTemplate)
                .expire(
                        eq(key),
                        eq(Duration.ofMinutes(15))
                );
    }

    // =========================================================
    // Redis failure fallback
    // =========================================================

    @Test
    void shouldFallbackToDatabaseWhenRedisIsUnavailable() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenThrow(
                        new RuntimeException(
                                "Redis unavailable"
                        )
                );

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of("USER_READ")
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );

        verify(jdbcTemplate)
                .queryForObject(
                        anyString(),
                        eq(Integer.class),
                        any(),
                        any()
                );

        verify(jdbcTemplate)
                .query(
                        anyString(),
                        any(org.springframework.jdbc.core.RowMapper.class),
                        any(),
                        any()
                );
    }

    @Test
    void shouldReturnDatabaseResultWhenRedisWriteFails() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of("USER_READ")
        );

        when(setOperations.add(
                eq(key),
                any(String[].class)
        )).thenThrow(
                new RuntimeException(
                        "Redis unavailable"
                )
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );
    }

    // =========================================================
    // Database failure
    // =========================================================

    @Test
    void shouldThrowServiceUnavailableWhenDatabaseFails() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenThrow(
                new RuntimeException(
                        "Database unavailable"
                )
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> resolver.resolvePermissions(
                                "user-001",
                                "tenant-001"
                        )
                );

        assertEquals(
                503,
                exception.getStatusCode().value()
        );

        assertTrue(
                exception.getReason()
                        .contains(
                                "RBAC database is currently unavailable"
                        )
        );
    }

    @Test
    void shouldNotWriteToRedisWhenDatabaseFails() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenThrow(
                new RuntimeException(
                        "Database unavailable"
                )
        );

        assertThrows(
                ResponseStatusException.class,
                () -> resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                )
        );

        verify(setOperations, never())
                .add(
                        anyString(),
                        any(String[].class)
                );

        verify(redisTemplate, never())
                .expire(
                        anyString(),
                        any(Duration.class)
                );
    }

    // =========================================================
    // Duplicate permissions
    // =========================================================

    @Test
    void shouldReturnUniquePermissions() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of(
                        "USER_READ",
                        "USER_READ",
                        "USER_UPDATE"
                )
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of(
                        "USER_READ",
                        "USER_UPDATE"
                ),
                result
        );

        assertEquals(
                2,
                result.size()
        );
    }

    // =========================================================
    // Null super-admin count
    // =========================================================

    @Test
    void shouldContinueWhenSuperAdminCountIsNull() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(),
                any()
        )).thenReturn(null);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                any(),
                any()
        )).thenReturn(
                List.of("USER_READ")
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );
    }

    // =========================================================
    // Single cached permission
    // =========================================================

    @Test
    void shouldReturnSingleCachedPermission() {

        String key =
                "perms:tenant-001:user-001";

        when(setOperations.members(key))
                .thenReturn(
                        Set.of("USER_READ")
                );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ"),
                result
        );

        verify(setOperations)
                .members(key);

        verifyNoInteractions(jdbcTemplate);
    }
}