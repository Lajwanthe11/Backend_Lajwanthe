package com.example.platformadmin.rbac.controller;

import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.config.PublicEndpoint;
import com.example.platformadmin.rbac.dto.request.PermissionCheckRequestDto;
import com.example.platformadmin.rbac.service.PermissionCacheService;
import com.example.platformadmin.rbac.service.PermissionCheckService;

import jakarta.validation.Valid;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class RbacPermissionController {

    private final PermissionCheckService permissionCheckService;
    private final PermissionCacheService permissionCacheService;
    private final RedisTemplate<String, String> redisTemplate;

    public RbacPermissionController(
            PermissionCheckService permissionCheckService,
            PermissionCacheService permissionCacheService,
            RedisTemplate<String, String> redisTemplate) {

        this.permissionCheckService = permissionCheckService;
        this.permissionCacheService = permissionCacheService;
        this.redisTemplate = redisTemplate;
    }

    // Checks whether the user has the requested permission.
    @PublicEndpoint(reason = "Internal RBAC permission service endpoint")
    @PostMapping("/auth/permissions/check")
    public ResponseEntity<Map<String, Boolean>> checkPermission(
            @Valid @RequestBody PermissionCheckRequestDto request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getClaimAsString("userId");
        String tenantId = jwt.getClaimAsString("tenantId");

        if (userId == null || userId.isBlank()
                || tenantId == null || tenantId.isBlank()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TenantContext.setTenantId(tenantId);

        try {
            boolean allowed = permissionCheckService.hasPermission(
                    userId,
                    request.getPermissionCode());

            return ResponseEntity.ok(
                    Map.of("allowed", allowed));

        } finally {
            TenantContext.clear();
        }
    }

    // Returns all permissions resolved for the user.
    @PublicEndpoint(reason = "Internal RBAC permission service endpoint")
    @GetMapping("/users/{userId}/permissions/resolved")
    public ResponseEntity<Set<String>> getResolvedPermissions(
            @PathVariable String userId,
            @RequestParam String tenantId) {

        Set<String> permissions =
                permissionCheckService.getResolvedPermissions(
                        userId,
                        tenantId);

        return ResponseEntity.ok(permissions);
    }

    // Clears the user's permission cache.
    @PublicEndpoint(reason = "Internal RBAC permission service endpoint")
    @PostMapping("/users/{userId}/permissions/cache/clear")
    public ResponseEntity<Void> clearPermissionCache(
            @PathVariable String userId,
            @RequestParam String tenantId) {

        permissionCacheService.clearUserPermissionsCache(
                userId,
                tenantId);

        return ResponseEntity.noContent().build();
    }

    // Checks whether the RBAC service can communicate with Redis.
    @PublicEndpoint(reason = "Health check endpoint")
    @GetMapping("/rbac/health")
    public ResponseEntity<Map<String, String>> health() {

        try {
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            return ResponseEntity.ok(
                    Map.of(
                            "status", "UP",
                            "redis", "UP"));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(
                            Map.of(
                                    "status", "DOWN",
                                    "redis", "DOWN"));
        }
    }
}