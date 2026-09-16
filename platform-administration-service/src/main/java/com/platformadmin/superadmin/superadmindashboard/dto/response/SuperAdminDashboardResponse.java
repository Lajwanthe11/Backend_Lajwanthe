package com.example.platformadmin.superadmin.superadmindashboard.dto.response;


import lombok.Data;

import java.util.List;


@Data

public class SuperAdminDashboardResponse {
    private PlatformSummaryResponse platformSummary;
    private OperationalStatisticsResponse operationalStatistics;
    private SecurityOverviewResponse securityOverview;
    private List<RecentActivityResponse> recentActivities;


  }