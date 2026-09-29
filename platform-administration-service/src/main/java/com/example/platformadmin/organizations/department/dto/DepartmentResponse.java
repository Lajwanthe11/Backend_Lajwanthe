package com.example.platformadmin.organizations.department.dto;

import lombok.Data;

/**
 * Response DTO returned for Department API calls.
 */
@Data
public class DepartmentResponse {

    private Long id;
    private String departmentCode;
    private String departmentName;
    private String description;
    private Boolean active;
}
