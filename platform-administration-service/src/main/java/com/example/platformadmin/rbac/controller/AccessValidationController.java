package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.config.RequirePermission;
import com.example.platformadmin.rbac.dto.request.AccessValidationRequest;
import com.example.platformadmin.rbac.service.PermissionResolver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Internal service-to-service endpoint to check if a given user has a specific permission.
@RestController
@RequestMapping("/api/v1/rbac")
public class AccessValidationController {

    // Resolves permissions for any userId passed in the request (not just the
    // caller).
    private final PermissionResolver permissionResolver;

    // Injects PermissionResolver dependency via constructor.
    public AccessValidationController(PermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    // POST /api/v1/rbac/access/validate — checks if the given userId has the
    // requested permission. Requires INTERNAL_SERVICE permission.
    @RequirePermission("INTERNAL_SERVICE")
    @PostMapping("/access/validate")
    public Map<String, Object> validate(@RequestBody AccessValidationRequest request) {
        boolean allowed = permissionResolver.hasPermission(request.userId(), request.permissionCode());
        return Map.of(
                "userId", request.userId(),
                "permissionCode", request.permissionCode(),
                "resourceType", request.resourceType() == null ? "" : request.resourceType(),
                "resourceId", request.resourceId() == null ? "" : request.resourceId(),
                "allowed", allowed);
    }
}
