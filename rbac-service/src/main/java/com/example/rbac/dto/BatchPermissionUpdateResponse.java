package com.example.rbac.dto;

import java.util.UUID;

public class BatchPermissionUpdateResponse {

    private UUID roleId;
    private int updatedCount;
    private String message;

    public BatchPermissionUpdateResponse() {
    }

    public BatchPermissionUpdateResponse(UUID roleId, int updatedCount, String message) {
        this.roleId = roleId;
        this.updatedCount = updatedCount;
        this.message = message;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}