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
            PermissionMatrixService permissionMatrixService
    ) {
        this.rolePermissionService = rolePermissionService;
        this.rolePermissionBatchService = rolePermissionBatchService;
        this.permissionMatrixService = permissionMatrixService;
    }

    @GetMapping
    public ResponseEntity<List<RolePermission>> getPermissions(
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                rolePermissionService.getPermissionsByRole(roleId)
        );
    }

    @PostMapping
    public ResponseEntity<RolePermission> grantPermission(
            @PathVariable Long roleId,
            @Valid @RequestBody PermissionGrantRequest request
    ) {
        String userId = "SYSTEM";

        if (!Boolean.TRUE.equals(request.getGranted())) {
            rolePermissionService.revokePermission(
                    roleId,
                    request.getPermissionId(),
                    userId
            );

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                rolePermissionService.grantPermission(
                        roleId,
                        request.getPermissionId(),
                        userId
                )
        );
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> revokePermission(
            @PathVariable Long roleId,
            @PathVariable UUID permissionId
    ) {
        String userId = "SYSTEM";

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<BatchPermissionUpdateResponse> updateBatch(
            @PathVariable Long roleId,
            @Valid @RequestBody BatchPermissionUpdateRequest request
    ) {
        String userId = "SYSTEM";

        return ResponseEntity.ok(
                rolePermissionBatchService.updatePermissions(
                        roleId,
                        request,
                        userId
                )
        );
    }

    @GetMapping("/grouped")
    public ResponseEntity<
            List<PermissionMatrixResponse.PermissionGroupRow>>
    getGroupedPermissions(
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                permissionMatrixService.getGroupedPermissions(roleId)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(
            IllegalArgumentException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(
            org.springframework.orm.ObjectOptimisticLockingFailureException.class
    )
    public ResponseEntity<String> handleConflict(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        "Permission matrix was modified by another user. " +
                        "Refresh the matrix and try again."
                );
    }
}