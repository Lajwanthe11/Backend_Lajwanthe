package com.platformadmin.superadmin.globaldashboard.dto.response;

public record GlobalDashboardStatusResponse(
        String overallStatus,
        long healthyServices,
        long degradedServices,
        long unavailableServices
) {
}