package com.example.platformadmin.superadmin.globaldashboard.service;

import com.example.platformadmin.superadmin.globaldashboard.dto.response.*;
import com.example.platformadmin.superadmin.globaldashboard.dto.response.*;
import com.example.platformadmin.superadmin.globaldashboard.exception.GlobalDashboardValidationException;
import com.example.platformadmin.superadmin.globaldashboard.integration.client.LicenseManagementClient;
import com.example.platformadmin.superadmin.globaldashboard.integration.client.PlatformHealthClient;
import com.example.platformadmin.superadmin.globaldashboard.integration.client.TenantManagementClient;
import com.example.platformadmin.superadmin.globaldashboard.integration.client.UserManagementClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GlobalDashboardServiceImpl implements GlobalDashboardService {

    private final TenantManagementClient tenantManagementClient;
    private final PlatformHealthClient platformHealthClient;
    private final LicenseManagementClient licenseManagementClient;
    private final UserManagementClient userManagementClient;

    public GlobalDashboardServiceImpl(
            TenantManagementClient tenantManagementClient,
            PlatformHealthClient platformHealthClient,
            LicenseManagementClient licenseManagementClient,
            UserManagementClient userManagementClient) {

        this.tenantManagementClient = tenantManagementClient;
        this.platformHealthClient = platformHealthClient;
        this.licenseManagementClient = licenseManagementClient;
        this.userManagementClient = userManagementClient;
    }

    @Override
    public GlobalDashboardResponse getDashboard() {

        var tenantSummary =
                tenantManagementClient.getTenantSummary();

        var platformHealth =
                platformHealthClient.getPlatformHealth();

        validatePlatformHealth(
                platformHealth.cpuUtilizationPercentage(),
                platformHealth.memoryUsagePercentage(),
                platformHealth.storageUtilizationPercentage(),
                platformHealth.apiRequests(),
                platformHealth.backgroundJobs(),
                platformHealth.failedJobs()
        );

        var licenseSummary =
                licenseManagementClient.getLicenseSummary();

        var userSummary =
                userManagementClient.getUserSummary();

        var summary = new GlobalDashboardSummaryResponse(
                tenantSummary.totalTenants(),
                tenantSummary.activeTenants(),
                userSummary.totalUsers(),
                userSummary.activeUsers(),
                userSummary.onlineUsers()
        );

        var metrics = new GlobalDashboardMetricsResponse(
                platformHealth.cpuUtilizationPercentage(),
                platformHealth.memoryUsagePercentage(),
                platformHealth.storageUtilizationPercentage(),
                platformHealth.apiRequests(),
                platformHealth.backgroundJobs(),
                platformHealth.failedJobs(),
                licenseSummary.activeLicenses()
        );

        var status = new GlobalDashboardStatusResponse(
                "HEALTHY",
                5,
                2,
                1
        );

        List<RecentActivityResponse> recentActivities =
                getRecentActivities();
        List<GlobalDashboardNotificationResponse> notifications =
                getNotifications();

        return new GlobalDashboardResponse(
                summary,
                metrics,
                status,
                recentActivities,
                notifications
        );
    }

    @Override
    public GlobalDashboardSummaryResponse getSummary() {

        var tenantSummary =
                tenantManagementClient.getTenantSummary();

        var userSummary =
                userManagementClient.getUserSummary();

        return new GlobalDashboardSummaryResponse(
                tenantSummary.totalTenants(),
                tenantSummary.activeTenants(),
                userSummary.totalUsers(),
                userSummary.activeUsers(),
                userSummary.onlineUsers()
        );
    }

    @Override
    public GlobalDashboardMetricsResponse getMetrics() {

        var platformHealth =
                platformHealthClient.getPlatformHealth();

        validatePlatformHealth(
                platformHealth.cpuUtilizationPercentage(),
                platformHealth.memoryUsagePercentage(),
                platformHealth.storageUtilizationPercentage(),
                platformHealth.apiRequests(),
                platformHealth.backgroundJobs(),
                platformHealth.failedJobs()
        );

        var licenseSummary =
                licenseManagementClient.getLicenseSummary();

        return new GlobalDashboardMetricsResponse(
                platformHealth.cpuUtilizationPercentage(),
                platformHealth.memoryUsagePercentage(),
                platformHealth.storageUtilizationPercentage(),
                platformHealth.apiRequests(),
                platformHealth.backgroundJobs(),
                platformHealth.failedJobs(),
                licenseSummary.activeLicenses()
        );
    }

    @Override
    public GlobalDashboardStatusResponse getStatus() {

        return new GlobalDashboardStatusResponse(
                "HEALTHY",
                5,
                2,
                1
        );
    }

    /*
     * Temporary stub data for Recent Activities.
     *
     * These values are intentionally non-zero/non-empty because
     * the actual activity/audit dependency is not being built
     * as part of our Global Dashboard work.
     */
    @Override
    public List<RecentActivityResponse> getRecentActivities() {

        LocalDateTime now = LocalDateTime.now();

        return List.of(
                new RecentActivityResponse(
                        "ACT-001",
                        "Tenant created successfully",
                        "admin@jsuite.com",
                        "Tenant Management",
                        now.minusMinutes(10)
                ),

                new RecentActivityResponse(
                        "ACT-002",
                        "User account activated",
                        "admin@jsuite.com",
                        "User Management",
                        now.minusMinutes(25)
                ),

                new RecentActivityResponse(
                        "ACT-003",
                        "License assigned to tenant",
                        "system",
                        "License Management",
                        now.minusMinutes(40)
                ),

                new RecentActivityResponse(
                        "ACT-004",
                        "Platform configuration updated",
                        "superadmin@jsuite.com",
                        "Platform Configuration",
                        now.minusHours(1)
                ),

                new RecentActivityResponse(
                        "ACT-005",
                        "System health check completed",
                        "system",
                        "Platform Health",
                        now.minusHours(2)
                )
        );
    }

    private void validatePlatformHealth(
            double cpu,
            double memory,
            double storage,
            long apiRequests,
            long backgroundJobs,
            long failedJobs) {

        if (cpu < 0 || cpu > 100) {
            throw new GlobalDashboardValidationException(
                    "CPU utilization must be between 0 and 100"
            );
        }

        if (memory < 0 || memory > 100) {
            throw new GlobalDashboardValidationException(
                    "Memory usage must be between 0 and 100"
            );
        }

        if (storage < 0 || storage > 100) {
            throw new GlobalDashboardValidationException(
                    "Storage utilization must be between 0 and 100"
            );
        }

        if (apiRequests < 0) {
            throw new GlobalDashboardValidationException(
                    "API requests cannot be negative"
            );
        }

        if (backgroundJobs < 0) {
            throw new GlobalDashboardValidationException(
                    "Background jobs cannot be negative"
            );
        }

        if (failedJobs < 0) {
            throw new GlobalDashboardValidationException(
                    "Failed jobs cannot be negative"
            );
        }
    }
    @Override
    public List<GlobalDashboardNotificationResponse> getNotifications() {

        LocalDateTime now = LocalDateTime.now();

        return List.of(
                new GlobalDashboardNotificationResponse(
                        "NOT-001",
                        "High CPU Usage",
                        "Platform CPU utilization is currently above the normal threshold.",
                        "WARNING",
                        now.minusMinutes(15)
                ),

                new GlobalDashboardNotificationResponse(
                        "NOT-002",
                        "Failed Background Jobs",
                        "Some background jobs have failed and require attention.",
                        "ERROR",
                        now.minusMinutes(30)
                ),

                new GlobalDashboardNotificationResponse(
                        "NOT-003",
                        "License Renewal Reminder",
                        "Several licenses are approaching their renewal date.",
                        "INFO",
                        now.minusHours(2)
                )
        );
    }
}