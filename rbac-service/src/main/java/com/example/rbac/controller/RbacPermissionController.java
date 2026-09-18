package com.example.rbac.controller;

import com.example.rbac.dto.PermissionCheckRequestDto;
import com.example.rbac.service.PermissionCacheService;
import com.example.rbac.service.PermissionCheckService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class RbacPermissionController {

    private final PermissionCheckService permissionCheckService;
    private final PermissionCacheService permissionCacheService;

    public RbacPermissionController(
            PermissionCheckService permissionCheckService,
            PermissionCacheService permissionCacheService) {

        this.permissionCheckService = permissionCheckService;
        this.permissionCacheService = permissionCacheService;
    }

    // Checks whether the user has the requested permission.
    @PostMapping("/auth/permissions/check")
    public ResponseEntity<Map<String, Boolean>> checkPermission(
            @Valid @RequestBody PermissionCheckRequestDto request) {

        boolean allowed = permissionCheckService.hasPermission(
                request.getUserId(),
                request.getTenantId(),
                request.getPermissionCode());

        return ResponseEntity.ok(
                Map.of("allowed", allowed));
    }

    // Returns all permissions resolved for the user.
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
    @PostMapping("/users/{userId}/permissions/cache/clear")
    public ResponseEntity<Void> clearPermissionCache(
            @PathVariable String userId,
            @RequestParam String tenantId) {

        permissionCacheService.clearUserPermissionsCache(
                userId,
                tenantId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rbac/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}