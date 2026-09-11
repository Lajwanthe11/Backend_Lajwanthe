package com.example.platformadmin.superadmin.platformconfiguration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO used to update the lifecycle status of a platform configuration.
 *
 * <p>The {@code status} field is represented as a {@link String} constrained to either
 * {@code "ACTIVE"} or {@code "INACTIVE"} via {@link Pattern} validation.</p>
 *
 * <p>The audit field {@code updatedBy} is optional; when omitted, the service layer
 * resolves the updater identity from {@code SecurityContextHolder} (defaulting to {@code "admin"}).</p>
 */
public record PlatformConfigurationStatusUpdateRequest(

        /**
         * New status for the platform configuration.
         */
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be either ACTIVE or INACTIVE")
        String status
) {
}