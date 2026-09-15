package com.example.rbac.controller;

import com.example.common.tenant.TenantContext;
import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.service.PermissionMatrixService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionMatrixController {

    private final PermissionMatrixService permissionMatrixService;

    public PermissionMatrixController(
            PermissionMatrixService permissionMatrixService) {
        this.permissionMatrixService = permissionMatrixService;
    }

    @GetMapping("/matrix")
    public ResponseEntity<PermissionMatrixResponse> getMatrix() {

        String tenantId = TenantContext.getTenantId();

        return ResponseEntity.ok(
                permissionMatrixService.getMatrix(tenantId));
    }
}