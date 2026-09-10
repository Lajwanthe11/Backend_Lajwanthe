package com.example.rbac.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BatchPermissionUpdateRequest {

    @NotEmpty
    @Valid
    private List<PermissionGrantRequest> permissions;

    @NotNull
    private Long expectedVersion;

    public BatchPermissionUpdateRequest() {
    }

    public List<PermissionGrantRequest> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionGrantRequest> permissions) {
        this.permissions = permissions;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Long expectedVersion) {
        this.expectedVersion = expectedVersion;
    }
}

