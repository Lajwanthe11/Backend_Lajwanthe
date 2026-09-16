package com.example.platformadmin.superadmin.license_management_service.dto.request;



import com.example.platformadmin.superadmin.license_management_service.enums.LicenseType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LicenseCreateRequest(

        @NotBlank(message = "License plan is required")
        String licensePlan,

        @NotNull(message = "License type is required")
        LicenseType licenseType,

        @NotNull(message = "Activation date is required")
        LocalDate activationDate,

        @NotNull(message = "Expiry date is required")
        @Future(message = "Expiry date must be in the future")
        LocalDate expiryDate
) {
}