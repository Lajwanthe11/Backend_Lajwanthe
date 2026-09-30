package com.example.platformadmin.superadmin.superadmindashboard.integration;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("superAdminHealthClientStub")
public class PlatformHealthClientStub implements PlatformHealthClient {

    private final JdbcTemplate jdbcTemplate;
    private final OperatingSystemMXBean osBean;

    public PlatformHealthClientStub(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public SystemHealth getSystemHealth() {
        // 1. Dynamic Database Health
        String dbStatus;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbStatus = "CONNECTED";
        } catch (Exception e) {
            dbStatus = "DISCONNECTED";
        }

        // 2. Dynamic Memory Usage %
        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        double memoryUsage = totalMemory > 0
                ? round(((double) (totalMemory - freeMemory) / totalMemory) * 100.0)
                : 0.0;

        // 3. Dynamic CPU Usage %
        double cpuLoad = osBean.getCpuLoad();
        double cpuUsage = cpuLoad >= 0 ? round(cpuLoad * 100.0) : 15.0;

        // 4. Dynamic Disk Storage Utilization %
        File root = new File(".");
        long totalSpace = root.getTotalSpace();
        long freeSpace = root.getFreeSpace();
        double storageUsage = totalSpace > 0
                ? round(((double) (totalSpace - freeSpace) / totalSpace) * 100.0)
                : 0.0;

        // 5. System Health Status
        String serverHealth = ("CONNECTED".equals(dbStatus) && cpuUsage < 90.0 && memoryUsage < 95.0)
                ? "HEALTHY"
                : "DEGRADED";

        return new SystemHealth(
                serverHealth,
                "RUNNING",
                dbStatus,
                storageUsage,
                cpuUsage,
                memoryUsage
        );
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}