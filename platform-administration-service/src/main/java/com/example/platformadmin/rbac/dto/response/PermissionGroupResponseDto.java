package com.example.platformadmin.rbac.dto.response;

import java.util.List;
import java.util.UUID;

public class PermissionGroupResponseDto {

    private UUID groupId;
    private String groupName;
    private String groupCode;
    private String module;
    private int displayOrder;
    private boolean active;
    private List<PermissionResponseDto> permissions;

    public PermissionGroupResponseDto() {
    }

    public PermissionGroupResponseDto(UUID groupId, String groupName, String groupCode, String module,
                                       int displayOrder, boolean active) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupCode = groupCode;
        this.module = module;
        this.displayOrder = displayOrder;
        this.active = active;
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

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<PermissionResponseDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponseDto> permissions) {
        this.permissions = permissions;
    }
}
