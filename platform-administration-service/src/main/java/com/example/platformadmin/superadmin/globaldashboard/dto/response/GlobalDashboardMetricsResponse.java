package com.example.platformadmin.superadmin.globaldashboard.dto.response;

public record GlobalDashboardMetricsResponse(
        double cpuUtilizationPercentage,
        double memoryUsagePercentage,
        double storageUtilizationPercentage,
        long apiRequests,
        long backgroundJobs,
        long failedJobs,
        long activeLicenses
) {
}