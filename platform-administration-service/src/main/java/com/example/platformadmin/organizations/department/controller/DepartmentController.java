package com.example.platformadmin.organizations.department.controller;

import com.example.common.abstracts.AbstractController;
import com.example.platformadmin.organizations.department.dto.DepartmentRequest;
import com.example.platformadmin.organizations.department.dto.DepartmentResponse;
import com.example.platformadmin.organizations.department.entity.Department;
import com.example.platformadmin.organizations.department.service.DepartmentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Department management.
 * Inherits full CRUD from {@link AbstractController}.
 */
@RestController
@RequestMapping("/departments")
@Tag(name = "Departments", description = "CRUD operations for Departments")
public class DepartmentController extends AbstractController<
        Department,
        Long,
        DepartmentRequest,
        DepartmentResponse> {

    private final DepartmentServiceImpl departmentService;

    public DepartmentController(DepartmentServiceImpl departmentService) {
        super(departmentService);
        this.departmentService = departmentService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search departments", description = "Case-insensitive search by department name")
    public ResponseEntity<List<DepartmentResponse>> searchDepartments(@RequestParam String departmentName) {
        return ResponseEntity.ok(departmentService.searchDepartments(departmentName));
    }
}
