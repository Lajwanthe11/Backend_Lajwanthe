package com.example.rbac.controller;

import com.example.rbac.dto.DeptScopeReportDto;
import com.example.rbac.service.DepartmentPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Department Scope Report", description = "Reporting for department permission scope")
public class DeptScopeReportController {

    private final DepartmentPermissionService departmentPermissionService;

    public DeptScopeReportController(DepartmentPermissionService departmentPermissionService) {
        this.departmentPermissionService = departmentPermissionService;
    }

    @GetMapping("/dept-scope/report")
    @Operation(summary = "Get department permission scope report")
    public ResponseEntity<List<DeptScopeReportDto>> getDeptScopeReport() {
        return ResponseEntity.ok(departmentPermissionService.getDeptScopeReport());
    }
}