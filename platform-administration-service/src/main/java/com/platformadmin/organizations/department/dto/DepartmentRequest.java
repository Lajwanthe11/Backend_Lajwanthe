package com.example.platformadmin.organizations.department.dto;

/**
 * Request DTO for creating or updating a Department.
 */
public class DepartmentRequest {

    private String departmentCode;
    private String departmentName;
    private String description;
    private Boolean active;

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
