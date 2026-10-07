package com.example.platformadmin.rbac.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeptScopeReportDto {

    private UUID userRoleId;
    private UUID departmentId;
    private LocalDateTime createdAt;
    private String createdBy;

    public DeptScopeReportDto(UUID userRoleId, UUID departmentId, LocalDateTime createdAt, String createdBy) {
        this.userRoleId = userRoleId;
        this.departmentId = departmentId;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public UUID getUserRoleId() {
        return userRoleId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}