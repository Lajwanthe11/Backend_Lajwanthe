package com.example.platformadmin.rbac.dto.response;

import java.util.UUID;

/**
 * Response DTO returned after a batch permission update.
 *
 * Contains the role that was updated, the number of permission
 * changes processed, and a result message.
 */
public class BatchPermissionUpdateResponse {

    /**
     * ID of the role whose permissions were updated.
     */
    private UUID roleId;

    /**
     * Number of permission updates processed in the batch.
     */
    private int updatedCount;

    /**
     * Result message returned to the client.
     */
    private String message;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public BatchPermissionUpdateResponse() {
    }

    /**
     * Creates a response containing the update result.
     *
     * @param roleId       ID of the updated role
     * @param updatedCount number of permissions processed
     * @param message      result message
     */
    public BatchPermissionUpdateResponse(
            UUID roleId,
            int updatedCount,
            String message) {
        this.roleId = roleId;
        this.updatedCount = updatedCount;
        this.message = message;
    }

    /**
     * Returns the role ID.
     */
    public UUID getRoleId() {
        return roleId;
    }

    /**
     * Sets the role ID.
     */
    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    /**
     * Returns the number of updated permissions.
     */
    public int getUpdatedCount() {
        return updatedCount;
    }

    /**
     * Sets the number of updated permissions.
     */
    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    /**
     * Returns the result message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the result message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
