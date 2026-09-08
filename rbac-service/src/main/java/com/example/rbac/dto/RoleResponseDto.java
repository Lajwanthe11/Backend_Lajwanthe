package com.example.rbac.dto;

import com.example.rbac.enums.RoleType;

import java.time.LocalDateTime;

public class RoleResponseDto {

    private Long id;
    private String roleName;
    private String roleCode;
    private RoleType roleType;
    private String description;
    private String status;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public RoleResponseDto() {
    }

    public RoleResponseDto(Long id,
                           String roleName,
                           String roleCode,
                           RoleType roleType,
                           String description,
                           String status,
                           Boolean isDeleted,
                           LocalDateTime createdAt,
                           String createdBy,
                           LocalDateTime updatedAt,
                           String updatedBy) {

        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.roleType = roleType;
        this.description = description;
        this.status = status;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}