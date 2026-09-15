package com.example.qa.sprint2.rbac;

import com.example.rbac.service.PermissionResolverImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionResolverTest {

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
                redisTemplate
        );

        lenient()
                .when(redisTemplate.opsForSet())
                .thenReturn(setOperations);
    }

    @Test
    void shouldReturnPermissionsFromRedisCache() {

        Set<String> cachedPermissions = Set.of(
                "USER_READ",
                "USER_CREATE",
                "USER_UPDATE"
        );

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(cachedPermissions);

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertNotNull(result);
        assertEquals(cachedPermissions, result);

        verify(setOperations)
                .members("perms:tenant-001:user-001");

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldNotQueryDatabaseWhenCacheHasPermissions() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of("USER_READ"));

        resolver.resolvePermissions(
                "user-001",
                "tenant-001"
        );

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldResolvePermissionsFromDatabaseWhenCacheMisses() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of(
                "USER_READ",
                "USER_UPDATE"
        ));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ", "USER_UPDATE"),
                result
        );

        verify(jdbcTemplate)
                .queryForObject(
                        anyString(),
                        eq(Integer.class),
                        eq("user-001"),
                        eq("tenant-001")
                );

        verify(jdbcTemplate)
                .query(
                        anyString(),
                        any(org.springframework.jdbc.core.RowMapper.class),
                        eq("user-001"),
                        eq("tenant-001")
                );
    }

    @Test
    void shouldResolveFromDatabaseWhenCacheIsEmpty() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of("USER_READ"));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(Set.of("USER_READ"), result);
    }

    @Test
    void shouldReturnWildcardPermissionForSuperAdmin() {

        when(setOperations.members(
                "perms:tenant-001:admin-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("admin-001"),
                eq("tenant-001")
        )).thenReturn(1);

        Set<String> result =
                resolver.resolvePermissions(
                        "admin-001",
                        "tenant-001"
                );

        assertEquals(Set.of("*"), result);

        verify(jdbcTemplate)
                .queryForObject(
                        anyString(),
                        eq(Integer.class),
                        eq("admin-001"),
                        eq("tenant-001")
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
    void shouldReturnEmptySetWhenDatabaseReturnsNoPermissions() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of());

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(setOperations, never())
                .add(anyString(), any(String[].class));

        verify(redisTemplate, never())
                .expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldStoreDatabasePermissionsInRedis() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of(
                "USER_READ",
                "USER_UPDATE"
        ));

        resolver.resolvePermissions(
                "user-001",
                "tenant-001"
        );

        verify(setOperations)
                .add(
                        eq("perms:tenant-001:user-001"),
                        any(String[].class)
                );

        verify(redisTemplate)
                .expire(
                        eq("perms:tenant-001:user-001"),
                        any(Duration.class)
                );
    }

    @Test
    void shouldUseCorrectTenantAndUserInCacheKey() {

        when(setOperations.members(
                "perms:tenant-ABC:user-XYZ"
        )).thenReturn(Set.of("USER_READ"));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-XYZ",
                        "tenant-ABC"
                );

        assertEquals(Set.of("USER_READ"), result);

        verify(setOperations)
                .members("perms:tenant-ABC:user-XYZ");
    }

    @Test
    void shouldKeepDifferentTenantsSeparatedInCacheKey() {

        when(setOperations.members(
                "perms:tenant-A:user-001"
        )).thenReturn(Set.of("TENANT_A_PERMISSION"));

        when(setOperations.members(
                "perms:tenant-B:user-001"
        )).thenReturn(Set.of("TENANT_B_PERMISSION"));

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

        assertEquals(Set.of("TENANT_A_PERMISSION"), tenantA);
        assertEquals(Set.of("TENANT_B_PERMISSION"), tenantB);

        verify(setOperations)
                .members("perms:tenant-A:user-001");

        verify(setOperations)
                .members("perms:tenant-B:user-001");
    }

    @Test
    void shouldFallbackToDatabaseWhenRedisIsUnavailable() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenThrow(
                new RuntimeException("Redis unavailable")
        );

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of("USER_READ"));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(Set.of("USER_READ"), result);
    }

    @Test
    void shouldReturnDatabaseResultWhenRedisWriteFails() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of("USER_READ"));

        when(setOperations.add(
                anyString(),
                any(String[].class)
        )).thenThrow(
                new RuntimeException("Redis unavailable")
        );

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(Set.of("USER_READ"), result);
    }

    @Test
    void shouldThrowServiceUnavailableWhenDatabaseFails() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenThrow(
                new RuntimeException("Database unavailable")
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

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenThrow(
                new RuntimeException("Database unavailable")
        );

        assertThrows(
                ResponseStatusException.class,
                () -> resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                )
        );

        verify(setOperations, never())
                .add(anyString(), any(String[].class));

        verify(redisTemplate, never())
                .expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldCheckSuperAdminBeforeNormalPermissionResolution() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of("USER_READ"));

        resolver.resolvePermissions(
                "user-001",
                "tenant-001"
        );

        verify(jdbcTemplate)
                .queryForObject(
                        anyString(),
                        eq(Integer.class),
                        eq("user-001"),
                        eq("tenant-001")
                );

        verify(jdbcTemplate)
                .query(
                        anyString(),
                        any(org.springframework.jdbc.core.RowMapper.class),
                        eq("user-001"),
                        eq("tenant-001")
                );
    }

    @Test
    void shouldReturnUniquePermissions() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(0);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of(
                "USER_READ",
                "USER_READ",
                "USER_UPDATE"
        ));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(
                Set.of("USER_READ", "USER_UPDATE"),
                result
        );

        assertEquals(2, result.size());
    }

    @Test
    void shouldKeepDifferentUsersSeparatedInCache() {

        when(setOperations.members(
                "perms:tenant-001:user-A"
        )).thenReturn(Set.of("USER_A_PERMISSION"));

        when(setOperations.members(
                "perms:tenant-001:user-B"
        )).thenReturn(Set.of("USER_B_PERMISSION"));

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

        assertEquals(Set.of("USER_A_PERMISSION"), userA);
        assertEquals(Set.of("USER_B_PERMISSION"), userB);

        verify(setOperations)
                .members("perms:tenant-001:user-A");

        verify(setOperations)
                .members("perms:tenant-001:user-B");
    }

    @Test
    void shouldContinueWhenSuperAdminCountIsNull() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of());

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(null);

        when(jdbcTemplate.query(
                anyString(),
                any(org.springframework.jdbc.core.RowMapper.class),
                eq("user-001"),
                eq("tenant-001")
        )).thenReturn(List.of("USER_READ"));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(Set.of("USER_READ"), result);
    }

    @Test
    void shouldReturnSingleCachedPermission() {

        when(setOperations.members(
                "perms:tenant-001:user-001"
        )).thenReturn(Set.of("USER_READ"));

        Set<String> result =
                resolver.resolvePermissions(
                        "user-001",
                        "tenant-001"
                );

        assertEquals(Set.of("USER_READ"), result);

        verify(setOperations)
                .members("perms:tenant-001:user-001");

        verifyNoInteractions(jdbcTemplate);
    }
}