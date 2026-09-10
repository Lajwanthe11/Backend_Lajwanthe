package com.example.rbac.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Service
public class PermissionResolverImpl implements PermissionResolver {
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public PermissionResolverImpl(
            JdbcTemplate jdbcTemplate,
            RedisTemplate<String, String> redisTemplate) {

        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Set<String> resolvePermissions(
            String userId,
            String tenantId) {

        String cacheKey = "perms:" + tenantId + ":" + userId;

        // Try Redis first
        try {
            Set<String> cachedPermissions = redisTemplate.opsForSet().members(cacheKey);

            if (cachedPermissions != null
                    && !cachedPermissions.isEmpty()) {

                return cachedPermissions;
            }
        } catch (Exception ignored) {
            // Redis unavailable - continue with database fallback
        }

        // Cache miss or Redis unavailable - query database
        Set<String> permissions;

        try {
            permissions = resolveFromDatabase(userId, tenantId);

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "RBAC database is currently unavailable",
                    e);
        }

        // Store permissions in Redis
        try {
            if (!permissions.isEmpty()) {

                redisTemplate.opsForSet().add(
                        cacheKey,
                        permissions.toArray(new String[0]));

                redisTemplate.expire(
                        cacheKey,
                        CACHE_TTL);
            }
        } catch (Exception ignored) {
            // Redis unavailable - DB result is still valid
        }

        return permissions;
    }

    // TODO: Verify user_roles and role_permissions column names
    // once the related team modules are available.
    private Set<String> resolveFromDatabase(
            String userId,
            String tenantId) {

        String superAdminSql = """
                SELECT COUNT(*)
                FROM user_roles ur
                INNER JOIN roles r
                    ON ur.role_id = r.id
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

        Integer superAdminCount = jdbcTemplate.queryForObject(
                superAdminSql,
                Integer.class,
                userId,
                tenantId);

        if (superAdminCount != null
                && superAdminCount > 0) {

            return Set.of("*");
        }

        String sql = """
                SELECT p.permission_code
                FROM permissions p
                INNER JOIN role_permissions rp
                    ON p.permission_id = rp.permission_id
                INNER JOIN user_roles ur
                    ON rp.role_id = ur.role_id
                INNER JOIN roles r
                    ON ur.role_id = r.id
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
                        (resultSet, rowNum) -> resultSet.getString("permission_code"),
                        userId,
                        tenantId));
    }
}
