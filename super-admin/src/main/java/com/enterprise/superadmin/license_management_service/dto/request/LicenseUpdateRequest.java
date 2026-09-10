package com.enterprise.superadmin.license_management_service.dto.request;


import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record LicenseUpdateRequest(

        @NotBlank(message = "License plan is required")
        String licensePlan,

        com.enterprise.superadmin.license_management_service.enums.LicenseType licenseType,

        LocalDate activationDate,

        LocalDate expiryDate
) {
}