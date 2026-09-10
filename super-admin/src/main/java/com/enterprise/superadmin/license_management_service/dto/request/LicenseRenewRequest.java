package com.enterprise.superadmin.license_management_service.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LicenseRenewRequest(

        @NotNull(message = "New expiry date is required")
        @Future(message = "New expiry date must be in the future")
        LocalDate newExpiryDate
) {
}