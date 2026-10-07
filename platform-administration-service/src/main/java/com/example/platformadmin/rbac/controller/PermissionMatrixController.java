package com.example.platformadmin.rbac.controller;

import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.dto.response.PermissionMatrixResponse;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionMatrixService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for the Permission Matrix.
 *
 * This controller provides the API used to load the complete
 * Permission Matrix for the current tenant.
 *
 * The matrix contains:
 * - Roles as columns
 * - Permission Groups as sections
 * - Permissions as rows
 * - Grant status for each role and permission
 */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionMatrixController {

    /*
     * Service responsible for building the Permission Matrix
     * response from roles, permissions and role-permission mappings.
     */
    private final PermissionMatrixService permissionMatrixService;

    /**
     * Constructor injection for PermissionMatrixService.
     */
    public PermissionMatrixController(
            PermissionMatrixService permissionMatrixService) {

        this.permissionMatrixService = permissionMatrixService;
    }

    /**
     * Get the Permission Matrix for the current tenant.
     *
     * Endpoint:
     * GET /api/v1/permissions/matrix
     *
     * The tenant ID is obtained from TenantContext.
     * The tenant ID is converted from String to UUID because
     * the PermissionMatrixService uses UUID for tenant identification.
     *
     * @return complete Permission Matrix
     */
    @GetMapping("/matrix")
    public ResponseEntity<PermissionMatrixResponse> getMatrix() {

        /*
         * TenantContext contains the tenant ID for the
         * current request.
         */
        String tenantId = TenantContext.getTenantId();

        /*
         * Validate that a tenant ID is available.
         */
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException(
                    "Tenant ID is not available");
        }

        /*
         * Convert the tenant ID from String to UUID.
         *
         * PermissionMatrixService.getMatrix() expects UUID.
         */
        UUID tenantUuid;

        try {
            tenantUuid = UUID.fromString(tenantId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Tenant ID must be a valid UUID: " + tenantId);
        }

        /*
         * Build and return the Permission Matrix.
         */
        return ResponseEntity.ok(
                permissionMatrixService.getMatrix(tenantUuid));
    }
}
