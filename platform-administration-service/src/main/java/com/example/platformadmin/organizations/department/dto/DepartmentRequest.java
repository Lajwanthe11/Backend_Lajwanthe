package com.example.platformadmin.organizations.department.dto;

import lombok.Data;

/**
 * Request DTO for creating or updating a Department.
 */
@Data
public class DepartmentRequest {

    private String departmentCode;
    private String departmentName;
    private String description;
    private Boolean active;
}
