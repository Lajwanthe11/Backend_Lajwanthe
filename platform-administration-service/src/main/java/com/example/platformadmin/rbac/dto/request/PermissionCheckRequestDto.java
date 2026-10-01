package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PermissionCheckRequestDto {

    @NotBlank
    private String permissionCode;

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
}