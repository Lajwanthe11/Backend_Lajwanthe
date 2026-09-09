package com.example.rbac.controller;

import com.example.microservice.common.tenant.TenantContext;
import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.service.PermissionMatrixService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionMatrixController {

    private final PermissionMatrixService permissionMatrixService;

    @GetMapping("/matrix")
    public ResponseEntity<PermissionMatrixResponse> getMatrix() {

        String tenantId =
                TenantContext.getTenantId();

        return ResponseEntity.ok(
                permissionMatrixService.getMatrix(
                        tenantId
                )
        );
    }
}