package com.example.platformadmin.superadmin.superadmindashboard.dto.response;


import lombok.Data;

import java.util.List;
import java.util.Map;


@Data

public class SuperAdminDashboardResponse {
    private PlatformSummaryResponse platformSummary;
    private OperationalStatisticsResponse operationalStatistics;
    private SecurityOverviewResponse securityOverview;
    private List<RecentActivityResponse> recentActivities;
    private List<DashboardAlertResponse> notificationsAndAlerts;
    private List<ModuleNavigationResponse> administrationModules;
    private Map<String, Object> licenseManagementSummary;
    private Map<String, Object> featureManagementSummary;


  }