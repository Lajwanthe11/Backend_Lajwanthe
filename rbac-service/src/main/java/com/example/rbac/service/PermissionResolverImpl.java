package com.example.rbac.service;

import com.example.common.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Profile("!dev")
public class PermissionResolverImpl implements PermissionResolver {

    private static final Logger log =
            LoggerFactory.getLogger(PermissionResolverImpl.class);

    private static final Duration CACHE_TTL =
            Duration.ofMinutes(15);

    private static final String NO_PERMISSIONS =
            "__NO_PERMISSIONS__";

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final Counter redisCacheFailures;

    public PermissionResolverImpl(
            JdbcTemplate jdbcTemplate,
            RedisTemplate<String, String> redisTemplate,
            MeterRegistry meterRegistry) {

        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;

        this.redisCacheFailures =
                Counter.builder("redis.cache.failures")
                        .description(
                                "Number of Redis permission cache failures")
                        .register(meterRegistry);
    }

    @Override
    public Set<String> resolvePermissions(
            String userId,
            String tenantId) {

        String cacheKey =
                "perms:" + tenantId + ":" + userId;

        // Check Redis first; fall back to DB if unavailable or on cache miss.
        try {
            Set<String> cachedPermissions =
                    redisTemplate.opsForSet().members(cacheKey);

            if (cachedPermissions != null
                    && !cachedPermissions.isEmpty()) {

                Set<String> permissions =
                        new HashSet<>(cachedPermissions);

                permissions.remove(NO_PERMISSIONS);

                return permissions;
            }

        } catch (Exception e) {

            log.warn(
                    "Redis permission cache unavailable, falling back to database",
                    e);

            redisCacheFailures.increment();
        }

        Set<String> permissions;

        try {
            permissions =
                    resolveFromDatabase(
                            userId,
                            tenantId);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid userId or tenantId",
                    e);

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "RBAC database is currently unavailable",
                    e);
        }

        // Cache the resolved permissions for 15 minutes.
        try {
            redisTemplate.delete(cacheKey);

            if (permissions.isEmpty()) {

                redisTemplate.opsForSet().add(
                        cacheKey,
                        NO_PERMISSIONS);

            } else {

                redisTemplate.opsForSet().add(
                        cacheKey,
                        permissions.toArray(new String[0]));
            }

            redisTemplate.expire(
                    cacheKey,
                    CACHE_TTL);

        } catch (Exception e) {

            log.warn(
                    "Failed to update Redis permission cache",
                    e);

            redisCacheFailures.increment();
        }

        return permissions;
    }

    private Set<String> resolveFromDatabase(
            String userId,
            String tenantId) {

        UUID userUuid =
                UUID.fromString(userId);

        UUID tenantUuid =
                UUID.fromString(tenantId);

        // SUPER_ADMIN gets all permissions.
        String superAdminSql = """
                SELECT COUNT(*)
                FROM user_roles ur
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE ur.user_id = ?
                  AND ur.tenant_id = ?
                  AND ur.is_active = true
                  AND ur.effective_date <= CURRENT_DATE
                  AND (ur.expiry_date IS NULL
                       OR ur.expiry_date > CURRENT_DATE)
                  AND r.role_code = 'SUPER_ADMIN'
                  AND r.status = 'ACTIVE'
                  AND r.is_deleted = false
                """;

        Integer superAdminCount =
                jdbcTemplate.queryForObject(
                        superAdminSql,
                        Integer.class,
                        userUuid,
                        tenantUuid);

        if (superAdminCount != null
                && superAdminCount > 0) {

            return Set.of("*");
        }

        // Resolve permissions from all valid role assignments.
        String sql = """
                SELECT p.permission_code
                FROM permissions p
                INNER JOIN role_permissions rp
                    ON p.permission_id = rp.permission_id
                INNER JOIN user_roles ur
                    ON rp.role_id = ur.role_id
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE ur.user_id = ?
                  AND ur.tenant_id = ?
                  AND ur.is_active = true
                  AND ur.effective_date <= CURRENT_DATE
                  AND (ur.expiry_date IS NULL
                       OR ur.expiry_date > CURRENT_DATE)
                  AND rp.is_active = true
                  AND p.is_active = true
                  AND r.status = 'ACTIVE'
                  AND r.is_deleted = false
                """;

        return new HashSet<>(
                jdbcTemplate.query(
                        sql,
                        (resultSet, rowNum) ->
                                resultSet.getString(
                                        "permission_code"),
                        userUuid,
                        tenantUuid));
    }

    @Override
    public boolean hasPermission(
            String userId,
            String permissionCode) {

        String tenantId =
                TenantContext.getTenantId();

        Set<String> permissions =
                resolvePermissions(
                        userId,
                        tenantId);

        return permissions.contains("*")
                || permissions.contains(permissionCode);
    }
}