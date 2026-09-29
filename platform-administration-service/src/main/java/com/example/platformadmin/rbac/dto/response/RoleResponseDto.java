package com.example.platformadmin.rbac.dto.response;

import com.example.platformadmin.rbac.enums.RoleType;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class RoleResponseDto {

    private UUID id;
    private String roleName;
    private String roleCode;
    private RoleType roleType;
    private String description;
    private String status;
    private Boolean isDeleted;
    private Set<String> permissionCodes;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    // Default constructor
    public RoleResponseDto() {
    }

    // Constructor used by RoleServiceImpl
    public RoleResponseDto(
            UUID id,
            String roleName,
            String roleCode,
            RoleType roleType,
            String description,
            String status,
            Boolean isDeleted,
            Set<String> permissionCodes,
            LocalDateTime createdAt,
            UUID createdBy,
            LocalDateTime updatedAt,
            UUID updatedBy) {

        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.roleType = roleType;
        this.description = description;
        this.status = status;
        this.isDeleted = isDeleted;
        this.permissionCodes = permissionCodes;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public RoleResponseDto(
            UUID id,
            String roleName,
            String roleCode,
            RoleType roleType,
            String description,
            String status,
            Boolean isDeleted,
            LocalDateTime createdAt,
            UUID createdBy,
            LocalDateTime updatedAt,
            UUID updatedBy) {

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


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Set<String> getPermissionCodes() { return permissionCodes; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}