package com.example.rbac.controller;

import com.example.rbac.dto.request.AssignRoleRequest;
import com.example.rbac.dto.request.RevokeRoleRequest;
import com.example.rbac.dto.response.UserRoleResponse;
import com.example.rbac.service.UserRoleService;
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

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<List<UserRoleResponse>> assignRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {

        return ResponseEntity.ok(
                userRoleService.assignRoles(userId, request)
        );
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<UserRoleResponse>> getCurrentRoles(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                userRoleService.getCurrentRoles(userId)
        );
    }

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

    @GetMapping("/users/{userId}/roles/history")
    public ResponseEntity<List<UserRoleResponse>> getRoleHistory(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                userRoleService.getRoleHistory(userId)
        );
    }

    @GetMapping("/roles/{roleId}/users")
    public ResponseEntity<List<UserRoleResponse>> getUsersByRole(
            @PathVariable UUID roleId) {

        return ResponseEntity.ok(
                userRoleService.getUsersByRole(roleId)
        );
    }
}