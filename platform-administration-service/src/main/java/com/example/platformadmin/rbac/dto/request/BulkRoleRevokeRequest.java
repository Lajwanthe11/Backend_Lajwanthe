package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BulkRoleRevokeRequest(

        @NotEmpty(message = "userIds must contain at least one user")
        @Size(
                max = 500,
                message = "A maximum of 500 users can be revoked in one request"
        )
        List<@NotNull UUID> userIds,

        @NotNull(message = "roleId is required")
        UUID roleId,

        @NotBlank(message = "reason is mandatory for bulk revoke")
        String reason
) {
}
