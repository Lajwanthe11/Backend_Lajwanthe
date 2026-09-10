package com.enterprise.superadmin.license_management_service.dto.response;




import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;

import java.time.LocalDate;
import java.util.UUID;

public record LicenseStatusResponse(

        UUID licenseId,

        String licenseKey,

        LicenseStatus status,

        LocalDate activationDate,

        LocalDate expiryDate,

        boolean expired
) {
}
