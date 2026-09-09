package com.example.rbac.controller;

import com.example.rbac.service.DepartmentPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Department Permissions", description = "Department scope management for user role assignments")
public class DepartmentPermissionController {

    private final DepartmentPermissionService departmentPermissionService;

    public DepartmentPermissionController(DepartmentPermissionService departmentPermissionService) {
        this.departmentPermissionService = departmentPermissionService;
    }

    // Get department scope for a users role
    @GetMapping("/{userId}/roles/{roleId}/departments")
    @Operation(summary = "Get department scope for a user's role")
    public ResponseEntity<List<Long>> getDepartmentScope(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(departmentPermissionService.getDepartmentScope(roleId));
    }

    // Update department scope for a users role
    @PutMapping("/{userId}/roles/{roleId}/departments")
    @Operation(summary = "Update department scope for a user's role")
    public ResponseEntity<Void> updateDepartmentScope(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @RequestBody Map<String, List<Long>> requestBody) {

        List<Long> departmentIds = requestBody.get("departmentIds");
        departmentPermissionService.updateDepartmentScope(roleId, departmentIds);
        return ResponseEntity.ok().build();
    }

    // Remove all department scope or revert to All Departments
    @DeleteMapping("/{userId}/roles/{roleId}/departments/all")
    @Operation(summary = "Remove all department scope for a user's role")
    public ResponseEntity<Void> removeAllDepartmentScope(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        departmentPermissionService.removeAllDepartmentScope(roleId);
        return ResponseEntity.ok().build();
    }
}