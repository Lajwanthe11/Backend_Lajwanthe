package com.example.rbac.controller;

import com.example.auth.security.user.UserPrincipal;
import com.example.rbac.dto.BatchPermissionUpdateRequest;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.dto.GrantPermissionRequest;
import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.service.PermissionMatrixService;
import com.example.rbac.service.RolePermissionBatchService;
import com.example.rbac.service.RolePermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles/{roleId}/permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    private final RolePermissionBatchService
            rolePermissionBatchService;

    private final PermissionMatrixService
            permissionMatrixService;

    @GetMapping
    public ResponseEntity<List<RolePermission>> getPermissions(
            @PathVariable Long roleId
    ) {

        return ResponseEntity.ok(
                rolePermissionService
                        .getPermissionsByRole(roleId)
        );
    }

    @PostMapping
    public ResponseEntity<RolePermission> grantPermission(
            @PathVariable Long roleId,
            @Valid @RequestBody GrantPermissionRequest request
    ) {

        String userId = getCurrentUserId();

        return ResponseEntity.ok(
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

        String userId = getCurrentUserId();

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<BatchPermissionUpdateResponse>
    updateBatch(
            @PathVariable Long roleId,
            @Valid @RequestBody BatchPermissionUpdateRequest request
    ) {

        String userId = getCurrentUserId();

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
                permissionMatrixService
                        .getGroupedPermissions(roleId)
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
    public ResponseEntity<String> handleConflict(
            Exception ex
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        "Permission matrix was modified by another user. " +
                        "Refresh the matrix and try again."
                );
    }

    private String getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof UserPrincipal)) {

            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        return principal.getId();
    }
}