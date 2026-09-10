package com.example.rbac.dto;

import java.util.UUID;

public class PermissionResponseDto {

    private Long permissionId;
    private String permissionCode;
    private String resource;
    private String action;
    private UUID groupId;
    private String groupName;
    private String displayName;
    private String description;
    private boolean active;
    private boolean system;
    private String module;

    public PermissionResponseDto() {
    }

    public PermissionResponseDto(Long permissionId, String permissionCode, String resource, String action,
                                 UUID groupId, String groupName, String displayName, String description,
                                 boolean active, boolean system, String module) {
        this.permissionId = permissionId;
        this.permissionCode = permissionCode;
        this.resource = resource;
        this.action = action;
        this.groupId = groupId;
        this.groupName = groupName;
        this.displayName = displayName;
        this.description = description;
        this.active = active;
        this.system = system;
        this.module = module;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
