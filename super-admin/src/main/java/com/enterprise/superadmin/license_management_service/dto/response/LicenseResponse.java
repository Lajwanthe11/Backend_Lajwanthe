package com.enterprise.superadmin.license_management_service.dto.response;




import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LicenseResponse(

        UUID id,

        String licenseKey,

        String licensePlan,

        LicenseType licenseType,

        LocalDate activationDate,

        LocalDate expiryDate,

        LicenseStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
