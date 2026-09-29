package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.dto.request.BatchPermissionUpdateRequest;
import com.example.platformadmin.rbac.dto.response.BatchPermissionUpdateResponse;
import com.example.platformadmin.rbac.dto.request.PermissionGrantRequest;
import com.example.platformadmin.rbac.dto.response.PermissionMatrixResponse;
import com.example.platformadmin.rbac.entity.RolePermission;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionMatrixService;
import com.example.platformadmin.rbac.service.serviceImpl.RolePermissionBatchService;
import com.example.platformadmin.rbac.service.serviceImpl.RolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing role-permission assignments.
 *
 * This controller provides APIs to:
 * - View permissions assigned to a role
 * - Grant a permission to a role
 * - Revoke a permission from a role
 * - Save multiple permission changes in one batch
 * - Get permissions in grouped format for the Permission Matrix
 */
@RestController
@RequestMapping("/api/v1/roles/{roleId}/permissions")
public class RolePermissionController {

        /*
         * Service responsible for individual role-permission operations
         * such as granting and revoking permissions.
         */
        private final RolePermissionService rolePermissionService;

        /*
         * Service responsible for processing multiple permission
         * changes together when the Permission Matrix is saved.
         */
        private final RolePermissionBatchService rolePermissionBatchService;

        /*
         * Service responsible for preparing Permission Matrix data,
         * including roles, permission groups, permissions and grants.
         */
        private final PermissionMatrixService permissionMatrixService;

        /**
         * Constructor injection for required services.
         */
        public RolePermissionController(
                        RolePermissionService rolePermissionService,
                        RolePermissionBatchService rolePermissionBatchService,
                        PermissionMatrixService permissionMatrixService) {

                this.rolePermissionService = rolePermissionService;
                this.rolePermissionBatchService = rolePermissionBatchService;
                this.permissionMatrixService = permissionMatrixService;
        }

        /**
         * Get all active permissions assigned to a role.
         *
         * Endpoint:
         * GET /api/v1/roles/{roleId}/permissions
         *
         * @param roleId role identifier
         * @return list of active role-permission mappings
         */
        @GetMapping
        public ResponseEntity<List<RolePermission>> getPermissions(
                        @PathVariable UUID roleId) {

                return ResponseEntity.ok(
                                rolePermissionService.getPermissionsByRole(roleId));
        }

        /**
         * Grant or revoke a single permission.
         *
         * Endpoint:
         * POST /api/v1/roles/{roleId}/permissions
         *
         * The request contains:
         * - permissionId
         * - granted
         *
         * When granted is true, the permission is granted.
         * When granted is false, the permission is revoked.
         *
         * @param roleId         role identifier
         * @param request        permission grant/revoke request
         * @param authentication authenticated user information
         * @return created RolePermission when permission is granted
         */
        @PostMapping
        public ResponseEntity<RolePermission> grantPermission(
                        @PathVariable UUID roleId,
                        @Valid @RequestBody PermissionGrantRequest request,
                        Authentication authentication) {

                /*
                 * Authentication.getName() is expected to contain
                 * the authenticated user's UUID.
                 */
                UUID userId = UUID.fromString(authentication.getName());

                /*
                 * If granted is false, revoke the permission instead
                 * of creating a new role-permission mapping.
                 */
                if (!Boolean.TRUE.equals(request.getGranted())) {

                        rolePermissionService.revokePermission(
                                        roleId,
                                        request.getPermissionId(),
                                        userId);

                        return ResponseEntity.noContent().build();
                }

                /*
                 * Grant the permission and return the created mapping.
                 */
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                rolePermissionService.grantPermission(
                                                                roleId,
                                                                request.getPermissionId(),
                                                                userId));
        }

        /**
         * Revoke a single permission from a role.
         *
         * Endpoint:
         * DELETE /api/v1/roles/{roleId}/permissions/{permissionId}
         *
         * @param roleId         role identifier
         * @param permissionId   permission identifier
         * @param authentication authenticated user information
         * @return HTTP 204 when the operation is completed
         */
        @DeleteMapping("/{permissionId}")
        public ResponseEntity<Void> revokePermission(
                        @PathVariable UUID roleId,
                        @PathVariable UUID permissionId,
                        Authentication authentication) {

                /*
                 * Identify the user who performed the revoke operation.
                 */
                UUID userId = UUID.fromString(authentication.getName());

                /*
                 * The service handles the actual revoke operation
                 * and audit creation.
                 */
                rolePermissionService.revokePermission(
                                roleId,
                                permissionId,
                                userId);

                return ResponseEntity.noContent().build();
        }

        /**
         * Save multiple permission changes in one request.
         *
         * This endpoint is used by the Permission Matrix Save operation.
         *
         * Endpoint:
         * PUT /api/v1/roles/{roleId}/permissions/batch
         *
         * The request contains a list of permission changes.
         * Each item contains:
         * - permissionId
         * - granted
         *
         * @param roleId         role identifier
         * @param request        batch permission update request
         * @param authentication authenticated user information
         * @return batch update result
         */
        @PutMapping("/batch")
        public ResponseEntity<BatchPermissionUpdateResponse> updateBatch(
                        @PathVariable UUID roleId,
                        @Valid @RequestBody BatchPermissionUpdateRequest request,
                        Authentication authentication) {

                /*
                 * Identify the authenticated user who is saving
                 * the Permission Matrix changes.
                 */
                UUID userId = UUID.fromString(authentication.getName());

                /*
                 * Pass all permission changes to the batch service.
                 *
                 * This keeps the controller responsible only for
                 * receiving the request and returning the response.
                 */
                return ResponseEntity.ok(
                                rolePermissionBatchService.updatePermissions(
                                                roleId,
                                                request,
                                                userId));
        }

        /**
         * Get permissions grouped by PermissionGroup.
         *
         * This endpoint is useful for displaying permissions
         * under collapsible groups in the Permission Matrix.
         *
         * Endpoint:
         * GET /api/v1/roles/{roleId}/permissions/grouped
         *
         * @param roleId role identifier
         * @return permission groups with their permissions
         */
        @GetMapping("/grouped")
        public ResponseEntity<List<PermissionMatrixResponse.PermissionGroupRow>> getGroupedPermissions(
                        @PathVariable UUID roleId) {

                return ResponseEntity.ok(
                                permissionMatrixService.getGroupedPermissions(roleId));
        }

        /**
         * Handle invalid arguments thrown by the service layer.
         *
         * Examples:
         * - Role not found
         * - Permission not found
         * - Inactive permission
         * - Permission cannot be revoked
         *
         * @param ex IllegalArgumentException thrown by the service
         * @return HTTP 400 Bad Request with the error message
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleBadRequest(
                        IllegalArgumentException ex) {

                return ResponseEntity
                                .badRequest()
                                .body(ex.getMessage());
        }
}
