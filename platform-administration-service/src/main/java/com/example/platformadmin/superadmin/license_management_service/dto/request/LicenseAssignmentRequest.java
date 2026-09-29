package com.example.platformadmin.superadmin.license_management_service.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LicenseAssignmentRequest(

        @NotNull(message = "Tenant ID is required")
        UUID tenantId,

        @NotNull(message = "Assigned by is required")
        UUID assignedBy
) {
}