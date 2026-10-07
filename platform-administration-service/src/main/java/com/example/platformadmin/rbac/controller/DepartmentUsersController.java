package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.service.DepartmentPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.platformadmin.rbac.config.RequirePermission;

import java.util.List;
import java.util.UUID;

// Separate controller since this endpoint lives under /departments, not /users
@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Department Users", description = "Lookup users by department and permission")
public class DepartmentUsersController {

    private final DepartmentPermissionService departmentPermissionService;

    public DepartmentUsersController(DepartmentPermissionService departmentPermissionService) {
        this.departmentPermissionService = departmentPermissionService;
    }

    @GetMapping("/{deptId}/users/by-permission")
    @RequirePermission("DEPT_READ")
    @Operation(summary = "Get users in a department who have a specific permission")
    public ResponseEntity<List<UUID>> getUsersByPermission(
            @PathVariable UUID deptId,
            @RequestParam String permissionCode) {

        return ResponseEntity.ok(departmentPermissionService.getUsersByPermission(deptId, permissionCode));
    }
}