package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.entity.Permission;

import com.example.platformadmin.rbac.dto.response.DeptScopeReportDto;
import com.example.platformadmin.rbac.service.DepartmentPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.platformadmin.rbac.config.RequirePermission;

import java.util.List;

// Separate controller since this endpoint lives under /permissions, not /users
@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Department Scope Report", description = "Reporting for department permission scope")
public class DeptScopeReportController {

    private final DepartmentPermissionService departmentPermissionService;

    public DeptScopeReportController(DepartmentPermissionService departmentPermissionService) {
        this.departmentPermissionService = departmentPermissionService;
    }

    @GetMapping("/dept-scope/report")
    @RequirePermission("DEPT_READ")
    @Operation(summary = "Get department permission scope report")
    public ResponseEntity<List<DeptScopeReportDto>> getDeptScopeReport() {
        return ResponseEntity.ok(departmentPermissionService.getDeptScopeReport());
    }
}