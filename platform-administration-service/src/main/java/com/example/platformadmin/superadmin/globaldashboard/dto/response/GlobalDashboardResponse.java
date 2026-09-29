package com.example.platformadmin.superadmin.globaldashboard.dto.response;

import java.util.List;

public record GlobalDashboardResponse(
        GlobalDashboardSummaryResponse summary,
        GlobalDashboardMetricsResponse metrics,
        GlobalDashboardStatusResponse status,
        List<RecentActivityResponse> recentActivities,
        List<GlobalDashboardNotificationResponse> notifications
) {

    /*
     * Keeps the older 3-argument constructor working.
     */
    public GlobalDashboardResponse(
            GlobalDashboardSummaryResponse summary,
            GlobalDashboardMetricsResponse metrics,
            GlobalDashboardStatusResponse status) {

        this(
                summary,
                metrics,
                status,
                List.of(),
                List.of()
        );
    }

    /*
     * Keeps the 4-argument constructor working
     * for the Recent Activities implementation.
     */
    public GlobalDashboardResponse(
            GlobalDashboardSummaryResponse summary,
            GlobalDashboardMetricsResponse metrics,
            GlobalDashboardStatusResponse status,
            List<RecentActivityResponse> recentActivities) {

        this(
                summary,
                metrics,
                status,
                recentActivities,
                List.of()
        );
    }
}