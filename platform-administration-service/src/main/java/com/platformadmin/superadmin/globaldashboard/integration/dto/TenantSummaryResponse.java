package com.platformadmin.superadmin.globaldashboard.integration.dto;

public record TenantSummaryResponse(
        long totalTenants,
        long activeTenants
) {
}