package com.example.platformadmin.rbac.dto.request;

import com.example.platformadmin.rbac.entity.Role;

import com.example.platformadmin.rbac.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public class RoleRequestDto {
  
    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 100, message = "Role name must be between 3 and 100 characters")
    private String roleName;

    private String roleCode;

    @NotNull(message = "Role type is required")
    private RoleType roleType;

    private String description;

    private String status = "ACTIVE";

    private UUID templateId;

    private Set<String> permissionCodes;
    public RoleRequestDto() {
    }

    public RoleRequestDto(String roleName, String roleCode,RoleType roleType, String description,String status) {
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.roleType = roleType;
        this.description = description;
        this.status = status != null ? status : "ACTIVE";
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

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public Set<String> getPermissionCodes() { return permissionCodes; }
    public void setPermissionCodes(Set<String> permissionCodes) { this.permissionCodes = permissionCodes; }
}