package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.dto.request.AssignRoleRequest;
import com.example.platformadmin.rbac.dto.request.RevokeRoleRequest;
import com.example.platformadmin.rbac.dto.response.UserRoleResponse;
import com.example.platformadmin.rbac.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * Assign one or more roles to a user.
     * POST /api/v1/users/{userId}/roles
     */
    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<List<UserRoleResponse>> assignRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {

        return ResponseEntity.ok(
                userRoleService.assignRoles(userId, request)
        );
    }

    /**
     * Get all currently active roles assigned to a user.
     * GET /api/v1/users/{userId}/roles
     */
    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<UserRoleResponse>> getCurrentRoles(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                userRoleService.getCurrentRoles(userId)
        );
    }

    /**
     * Revoke a specific role from a user.
     * DELETE /api/v1/users/{userId}/roles/{roleId}
     */
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> revokeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId,
            @Valid @RequestBody RevokeRoleRequest request) {

        userRoleService.revokeRole(
                userId,
                roleId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Set a specific role as the primary role for a user.
     * PUT /api/v1/users/{userId}/roles/{roleId}/primary
     */
    @PutMapping("/users/{userId}/roles/{roleId}/primary")
    public ResponseEntity<Void> setPrimaryRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        userRoleService.setPrimaryRole(
                userId,
                roleId
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Get the full role assignment history for a user, including revoked roles.
     * GET /api/v1/users/{userId}/roles/history
     */
    @GetMapping("/users/{userId}/roles/history")
    public ResponseEntity<List<UserRoleResponse>> getRoleHistory(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                userRoleService.getRoleHistory(userId)
        );
    }

    /**
     * Get all users currently assigned to a specific role.
     * GET /api/v1/roles/{roleId}/users
     */
    @GetMapping("/roles/{roleId}/users")
    public ResponseEntity<List<UserRoleResponse>> getUsersByRole(
            @PathVariable UUID roleId) {

        return ResponseEntity.ok(
                userRoleService.getUsersByRole(roleId)
        );
    }
}