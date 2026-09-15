package com.example.rbac.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class PermissionGrantRequest {

    @NotNull
    private UUID permissionId;

    @NotNull
    private Boolean granted;

    public PermissionGrantRequest() {
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }

    public Boolean getGranted() {
        return granted;
    }

    public void setGranted(Boolean granted) {
        this.granted = granted;
    }
}