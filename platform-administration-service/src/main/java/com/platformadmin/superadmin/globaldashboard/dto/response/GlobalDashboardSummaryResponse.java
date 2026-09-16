package com.platformadmin.superadmin.globaldashboard.dto.response;

public record GlobalDashboardSummaryResponse(
        long totalTenants,
        long activeTenants,
        long totalUsers,
        long activeUsers,
        long onlineUsers
) {
}