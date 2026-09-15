package com.example.platformadmin.organizations.department.service;

import com.example.platformadmin.organizations.department.dto.DepartmentResponse;

import java.util.List;

/**
 * Department-specific service operations beyond base CRUD.
 */
public interface DepartmentService {

    List<DepartmentResponse> searchDepartments(String departmentName);
}
