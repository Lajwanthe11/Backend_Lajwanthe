package com.example.rbac.controller;

import com.example.rbac.config.RequirePermission;
import com.example.rbac.dto.AccessValidationRequest;
import com.example.rbac.service.PermissionResolver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * {@code POST /api/v1/rbac/access/validate} - "Validate if a specific user
 * has access to a resource (internal)".
 * <p>
 * Service-to-service endpoint: it evaluates access for whichever
 * {@code userId} is passed in the request body, NOT the caller's own JWT
 * identity. To keep this from becoming a way for an arbitrary end user to
 * probe other users' permissions, it is itself gated behind an
 * {@code INTERNAL_SERVICE} permission - only a trusted service account
 * should hold that.
 */
@RestController
@RequestMapping("/api/v1/rbac")
public class AccessValidationController {

    private final PermissionResolver permissionResolver;

    public AccessValidationController(PermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

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
