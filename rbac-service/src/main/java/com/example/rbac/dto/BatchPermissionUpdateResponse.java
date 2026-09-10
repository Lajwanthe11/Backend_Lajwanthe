package com.example.rbac.dto;

public class BatchPermissionUpdateResponse {

    private Long roleId;
    private int updatedCount;
    private String message;

    public BatchPermissionUpdateResponse() {
    }

    public BatchPermissionUpdateResponse(Long roleId, int updatedCount, String message) {
        this.roleId = roleId;
        this.updatedCount = updatedCount;
        this.message = message;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
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
