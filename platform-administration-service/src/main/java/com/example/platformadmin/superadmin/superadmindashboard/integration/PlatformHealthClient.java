package com.example.platformadmin.superadmin.superadmindashboard.integration;

public interface PlatformHealthClient {
    SystemHealth getSystemHealth();

    record SystemHealth(
            String serverHealth,        // e.g. "HEALTHY", "DEGRADED", "DOWN"
            String apiStatus,           // e.g. "RUNNING", "DOWN"
            String databaseStatus,      // e.g. "CONNECTED", "DISCONNECTED"
            double storageUtilizationPercent,
            double cpuUsagePercent,
            double memoryUsagePercent
    ) {
    }
}


