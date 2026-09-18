package com.example.rbac.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BulkRoleAssignmentRequest(

        @NotEmpty(message = "userIds must contain at least one user")
        @Size(
                max = 500,
                message = "A maximum of 500 users can be assigned in one request"
        )
        List<@NotNull UUID> userIds,

        @NotNull(message = "roleId is required")
        UUID roleId,

        @NotNull(message = "effectiveDate is required")
        LocalDate effectiveDate,

        LocalDate expiryDate,

        String reason
) {
}