package com.example.rbac.controller;

import com.example.rbac.dto.BatchPermissionUpdateRequest;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.dto.PermissionGrantRequest;
import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.service.PermissionMatrixService;
import com.example.rbac.service.RolePermissionBatchService;
import com.example.rbac.service.RolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles/{roleId}/permissions")
public class RolePermissionController {

        private final RolePermissionService rolePermissionService;
        private final RolePermissionBatchService rolePermissionBatchService;
        private final PermissionMatrixService permissionMatrixService;

        public RolePermissionController(
                        RolePermissionService rolePermissionService,
                        RolePermissionBatchService rolePermissionBatchService,
                        PermissionMatrixService permissionMatrixService) {
                this.rolePermissionService = rolePermissionService;
                this.rolePermissionBatchService = rolePermissionBatchService;
                this.permissionMatrixService = permissionMatrixService;
        }

        @GetMapping
        public ResponseEntity<List<RolePermission>> getPermissions(
                        @PathVariable UUID roleId) {
                return ResponseEntity.ok(
                                rolePermissionService.getPermissionsByRole(roleId));
        }

        @PostMapping
        public ResponseEntity<RolePermission> grantPermission(
                        @PathVariable UUID roleId,
                        @Valid @RequestBody PermissionGrantRequest request,
                        Authentication authentication) {

                String userId = authentication.getName();

                if (!Boolean.TRUE.equals(request.getGranted())) {
                        rolePermissionService.revokePermission(
                                        roleId,
                                        request.getPermissionId(),
                                        userId);

                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                rolePermissionService.grantPermission(
                                                roleId,
                                                request.getPermissionId(),
                                                userId));
        }

        @DeleteMapping("/{permissionId}")
        public ResponseEntity<Void> revokePermission(
                        @PathVariable UUID roleId,
                        @PathVariable UUID permissionId,
                        Authentication authentication) {

                String userId = authentication.getName();

                rolePermissionService.revokePermission(
                                roleId,
                                permissionId,
                                userId);

                return ResponseEntity.noContent().build();
        }

        @PutMapping("/batch")
        public ResponseEntity<BatchPermissionUpdateResponse> updateBatch(
                        @PathVariable UUID roleId,
                        @Valid @RequestBody BatchPermissionUpdateRequest request,
                        Authentication authentication) {

                String userId = authentication.getName();

                return ResponseEntity.ok(
                                rolePermissionBatchService.updatePermissions(
                                                roleId,
                                                request,
                                                userId));
        }

        @GetMapping("/grouped")
        public ResponseEntity<List<PermissionMatrixResponse.PermissionGroupRow>> getGroupedPermissions(
                        @PathVariable UUID roleId) {
                return ResponseEntity.ok(
                                permissionMatrixService.getGroupedPermissions(roleId));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleBadRequest(
                        IllegalArgumentException ex) {
                return ResponseEntity
                                .badRequest()
                                .body(ex.getMessage());
        }

        @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
        public ResponseEntity<String> handleConflict(Exception ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(
                                                "Permission matrix was modified by another user. " +
                                                                "Refresh the matrix and try again.");
        }
}