package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PermissionCheckRequestDto {

    @NotBlank
    private String userId;

    @NotBlank
    private String tenantId;

    @NotBlank
    private String permissionCode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
}