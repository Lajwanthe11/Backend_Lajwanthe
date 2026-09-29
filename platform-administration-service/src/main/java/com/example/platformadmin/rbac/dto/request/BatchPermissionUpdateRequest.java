package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO used to update multiple permissions for a role in one batch.
 *
 * The Permission Matrix sends the complete set of permission changes
 * when the user clicks the Save Matrix button.
 */
public class BatchPermissionUpdateRequest {

    /**
     * List of permission grant/revoke requests.
     *
     * @NotEmpty ensures that at least one permission update is provided.
     *
     * @Valid ensures that validation rules defined inside
     *        PermissionGrantRequest are also applied.
     */
    @NotEmpty
    @Valid
    private List<PermissionGrantRequest> permissions;

    /**
     * Default constructor required for request deserialization.
     */
    public BatchPermissionUpdateRequest() {
    }

    /**
     * Returns the permission updates.
     */
    public List<PermissionGrantRequest> getPermissions() {
        return permissions;
    }

    /**
     * Sets the permission updates.
     */
    public void setPermissions(List<PermissionGrantRequest> permissions) {
        this.permissions = permissions;
    }
}
