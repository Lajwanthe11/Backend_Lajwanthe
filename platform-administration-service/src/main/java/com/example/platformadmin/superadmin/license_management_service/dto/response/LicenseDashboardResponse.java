package com.example.platformadmin.superadmin.license_management_service.dto.response;

import java.math.BigDecimal;

public record LicenseDashboardResponse(

        long totalLicenses,

        long activeLicenses,

        long expiringLicenses,

        long suspendedLicenses,

        BigDecimal utilizationRate
) {
}