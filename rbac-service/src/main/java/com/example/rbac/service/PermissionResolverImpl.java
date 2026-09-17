package com.example.rbac.service;

import com.example.common.tenant.TenantContext;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.entity.UserRole;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private final EntityManager entityManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final Counter redisCacheFailures;

    public PermissionResolverImpl(
            EntityManager entityManager,
            RedisTemplate<String, String> redisTemplate,
            MeterRegistry meterRegistry) {

        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;

        this.redisCacheFailures =
                Counter.builder("redis.cache.failures")
                        .description(
                                "Number of Redis permission cache failures")
                        .register(meterRegistry);
    }

    @Override
    // Gets the user's permissions from cache or database.
    public Set<String> resolvePermissions(
            String userId,
            String tenantId) {

        String cacheKey =
                "perms:" + tenantId + ":" + userId;

        // Check Redis first; fall back to database on cache miss/failure.
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

    // Gets the user's active permissions from the database.
    Set<String> resolveFromDatabase(
            String userId,
            String tenantId) {

        UUID userUuid =
                UUID.fromString(userId);

        UUID tenantUuid =
                UUID.fromString(tenantId);

        LocalDate today =
                LocalDate.now();

        CriteriaBuilder criteriaBuilder =
                entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> superAdminQuery =
                criteriaBuilder.createQuery(Long.class);

        Root<UserRole> userRole =
                superAdminQuery.from(UserRole.class);

        Root<Role> role =
                superAdminQuery.from(Role.class);

        List<Predicate> superAdminPredicates =
                new ArrayList<>();

        superAdminPredicates.add(
                criteriaBuilder.equal(
                        userRole.get("roleId"),
                        role.get("id")));

        superAdminPredicates.add(
                criteriaBuilder.equal(
                        userRole.get("userId"),
                        userUuid));

        superAdminPredicates.add(
                criteriaBuilder.equal(
                        userRole.get("tenantId"),
                        tenantUuid));

        superAdminPredicates.add(
                criteriaBuilder.isTrue(
                        userRole.get("active")));

        superAdminPredicates.add(
                criteriaBuilder.lessThanOrEqualTo(
                        userRole.get("effectiveDate"),
                        today));

        superAdminPredicates.add(
                criteriaBuilder.or(
                        criteriaBuilder.isNull(
                                userRole.get("expiryDate")),
                        criteriaBuilder.greaterThan(
                                userRole.get("expiryDate"),
                                today)));

        superAdminPredicates.add(
                criteriaBuilder.equal(
                        role.get("roleCode"),
                        "SUPER_ADMIN"));

        superAdminPredicates.add(
                criteriaBuilder.equal(
                        role.get("status"),
                        "ACTIVE"));

        superAdminPredicates.add(
                criteriaBuilder.isFalse(
                        role.get("isDeleted")));

        superAdminQuery
                .select(
                        criteriaBuilder.count(userRole))
                .where(
                        superAdminPredicates.toArray(
                                new Predicate[0]));

        Long superAdminCount =
                entityManager
                        .createQuery(superAdminQuery)
                        .getSingleResult();

        if (superAdminCount != null
                && superAdminCount > 0) {

            return Set.of("*");
        }

        CriteriaQuery<String> permissionQuery =
                criteriaBuilder.createQuery(String.class);

        Root<UserRole> assignment =
                permissionQuery.from(UserRole.class);

        Root<Role> assignedRole =
                permissionQuery.from(Role.class);

        Root<RolePermission> rolePermission =
                permissionQuery.from(RolePermission.class);

        Root<Permission> permission =
                permissionQuery.from(Permission.class);

        List<Predicate> permissionPredicates =
                new ArrayList<>();

        permissionPredicates.add(
                criteriaBuilder.equal(
                        assignment.get("roleId"),
                        assignedRole.get("id")));

        permissionPredicates.add(
                criteriaBuilder.equal(
                        rolePermission
                                .get("role")
                                .get("id"),
                        assignedRole.get("id")));

        permissionPredicates.add(
                criteriaBuilder.equal(
                        rolePermission
                                .get("permission")
                                .get("permissionId"),
                        permission.get("permissionId")));

        permissionPredicates.add(
                criteriaBuilder.equal(
                        assignment.get("userId"),
                        userUuid));

        permissionPredicates.add(
                criteriaBuilder.equal(
                        assignment.get("tenantId"),
                        tenantUuid));

        permissionPredicates.add(
                criteriaBuilder.isTrue(
                        assignment.get("active")));

        permissionPredicates.add(
                criteriaBuilder.lessThanOrEqualTo(
                        assignment.get("effectiveDate"),
                        today));

        permissionPredicates.add(
                criteriaBuilder.or(
                        criteriaBuilder.isNull(
                                assignment.get("expiryDate")),
                        criteriaBuilder.greaterThan(
                                assignment.get("expiryDate"),
                                today)));

        permissionPredicates.add(
                criteriaBuilder.isTrue(
                        rolePermission.get("active")));

        permissionPredicates.add(
                criteriaBuilder.isTrue(
                        permission.get("active")));

        permissionPredicates.add(
                criteriaBuilder.equal(
                        assignedRole.get("status"),
                        "ACTIVE"));

        permissionPredicates.add(
                criteriaBuilder.isFalse(
                        assignedRole.get("isDeleted")));

        permissionQuery
                .select(
                        permission.get("permissionCode"))
                .distinct(true)
                .where(
                        permissionPredicates.toArray(
                                new Predicate[0]));

        List<String> permissionCodes =
                entityManager
                        .createQuery(permissionQuery)
                        .getResultList();

        return new HashSet<>(permissionCodes);
    }

    @Override
    // Checks if the user has the given permission.
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