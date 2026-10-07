package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.entity.Role;

import com.example.platformadmin.rbac.service.DepartmentPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.platformadmin.rbac.config.RequirePermission;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Department Permissions", description = "Department scope management for user role assignments")
public class DepartmentPermissionController {

    private final DepartmentPermissionService departmentPermissionService;

    public DepartmentPermissionController(DepartmentPermissionService departmentPermissionService) {
        this.departmentPermissionService = departmentPermissionService;
    }

    @GetMapping("/{userId}/roles/{roleId}/departments")
    @RequirePermission("DEPT_READ")
    @Operation(summary = "Get department scope for a user's role")
    public ResponseEntity<List<UUID>> getDepartmentScope(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        return ResponseEntity.ok(departmentPermissionService.getDepartmentScope(roleId));
    }

    // userId included in path for API consistency, not currently used in logic
    @PutMapping("/{userId}/roles/{roleId}/departments")
    @RequirePermission("ROLE_ASSIGN")
    @Operation(summary = "Update department scope for a user's role")
    public ResponseEntity<Void> updateDepartmentScope(
            @PathVariable UUID userId,
            @PathVariable UUID roleId,
            @RequestBody Map<String, List<UUID>> requestBody) {

        List<UUID> departmentIds = requestBody.get("departmentIds");
        departmentPermissionService.updateDepartmentScope(roleId, departmentIds);
        return ResponseEntity.ok().build();
    }

    // Reverts role assignment to "All Departments" (default when no scope rows exist)
    @DeleteMapping("/{userId}/roles/{roleId}/departments/all")
    @RequirePermission("ROLE_ASSIGN")
    @Operation(summary = "Remove all department scope for a user's role")
    public ResponseEntity<Void> removeAllDepartmentScope(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        departmentPermissionService.removeAllDepartmentScope(roleId);
        return ResponseEntity.ok().build();
    }
}