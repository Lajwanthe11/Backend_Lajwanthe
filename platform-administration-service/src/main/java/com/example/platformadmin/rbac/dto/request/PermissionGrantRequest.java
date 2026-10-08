package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Represents a single permission grant or revoke operation.
 *
 * This DTO is used inside BatchPermissionUpdateRequest.
 * Each item tells the backend which permission should be
 * granted or revoked for the selected role.
 */
public class PermissionGrantRequest {

    /**
     * ID of the permission being updated.
     *
     * @NotNull ensures that every permission update
     *          contains a valid permission ID.
     */
    @NotNull
    private UUID permissionId;

    /**
     * Indicates whether the permission should be granted.
     *
     * true = grant the permission.
     * false = revoke the permission.
     *
     * @NotNull prevents the request from omitting the
     *          grant/revoke decision.
     */
    @NotNull
    private Boolean granted;

    /**
     * Default constructor required for request deserialization.
     */
    public PermissionGrantRequest() {
    }

    /**
     * Returns the permission ID.
     */
    public UUID getPermissionId() {
        return permissionId;
    }

    /**
     * Sets the permission ID.
     */
    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }

    /**
     * Returns whether the permission should be granted.
     */
    public Boolean getGranted() {
        return granted;
    }

    /**
     * Sets whether the permission should be granted.
     */
    public void setGranted(Boolean granted) {
        this.granted = granted;
    }
}
