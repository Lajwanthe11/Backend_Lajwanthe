package com.example.platformadmin.superadmin.superadmindashboard.dto.response;


import com.example.platformadmin.superadmin.superadmindashboard.integration.UserManagementClient;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data

public class SuperAdminDashboardResponse {
    private PlatformSummaryResponse platformSummary;
    private DashboardStatisticsResponse systemStatus;
    private OperationalStatisticsResponse operationalStatistics;
    private SecurityOverviewResponse securityOverview;
    private List<RecentActivityResponse> recentActivities;
    private List<DashboardAlertResponse> notificationsAndAlerts;
    private List<ModuleNavigationResponse> administrationModules;
    private List<UserManagementClient.LoginActivityRecord> recentLoginActivities;
    private Map<String, Object> licenseManagementSummary;
    private Map<String, Object> featureManagementSummary;


  }