package com.platformadmin.superadmin.globaldashboard.service;

import com.example.platformadmin.superadmin.globaldashboard.dto.response.*;
import com.platformadmin.superadmin.globaldashboard.dto.response.*;

import java.util.List;


public interface GlobalDashboardService {

    GlobalDashboardResponse getDashboard();
    GlobalDashboardSummaryResponse getSummary();

    GlobalDashboardMetricsResponse getMetrics();

    GlobalDashboardStatusResponse getStatus();
    List<RecentActivityResponse> getRecentActivities();

    List<GlobalDashboardNotificationResponse> getNotifications();
}