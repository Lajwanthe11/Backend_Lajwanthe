package com.example.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class CustomRoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    private String roleName;

    @Size(max = 50, message = "Role code cannot exceed 50 characters")
    private String roleCode;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private List<UUID> permissionIds;

    @Size(max = 1000, message = "Publish notes cannot exceed 1000 characters")
    private String publishNotes;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UUID> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<UUID> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public String getPublishNotes() {
        return publishNotes;
    }

    public void setPublishNotes(String publishNotes) {
        this.publishNotes = publishNotes;
    }
}