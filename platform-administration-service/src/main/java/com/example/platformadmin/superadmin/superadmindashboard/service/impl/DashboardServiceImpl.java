package com.example.platformadmin.superadmin.superadmindashboard.service.impl;

import com.example.platformadmin.superadmin.superadmindashboard.dto.response.*;
import com.example.platformadmin.superadmin.superadmindashboard.integration.*;
import com.example.platformadmin.superadmin.superadmindashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final PlatformHealthClient healthClient;
    private final UserManagementClient userClient;
    private final OrganizationManagementClient orgClient;
    private final LicenseManagementClient licenseClient;

    public DashboardServiceImpl(
            @Qualifier("superAdminHealthClientStub") PlatformHealthClient healthClient,
            @Qualifier("superAdminUserClientStub") UserManagementClient userClient,
            OrganizationManagementClient orgClient,
            @Qualifier("superAdminLicenseClientStub") LicenseManagementClient licenseClient) {
        this.healthClient = healthClient;
        this.userClient = userClient;
        this.orgClient = orgClient;
        this.licenseClient = licenseClient;
    }

    @Override
    public SuperAdminDashboardResponse getDashboard() {
        var health = healthClient.getSystemHealth();
        var userStats = userClient.getUserStatistics();
        var orgStats = orgClient.getOrganizationStatistics();
        var licenseStats = licenseClient.getLicenseStatistics();
        var loginActivities = userClient.getRecentLoginActivities(5);

        // 1. Enterprise Platform Overview (#1)
        PlatformSummaryResponse summary = new PlatformSummaryResponse();
        summary.setTotalUsers(userStats.totalUsers());
        summary.setUserGrowthPercent(1.8);
        summary.setTotalTenants(orgStats.totalOrganizations());
        summary.setActiveTenants(orgStats.totalOrganizations());
        summary.setTotalOrganizations(orgStats.totalOrganizations());
        summary.setOrgGrowthPercent(4.2);
        summary.setActiveUsers(userStats.activeUsers());
        summary.setOnlineUsers(userStats.onlineUsers());
        summary.setPlatformStatus(health.serverHealth());
        // 2. System Status (Server, Database, API Gateway, Storage)
        DashboardStatisticsResponse systemStatus = DashboardStatisticsResponse.builder()
                .serverHealth(health.serverHealth())
                .apiGatewayStatus(health.apiStatus())                 // "RUNNING"
                .databaseStatus(health.databaseStatus())             // "CONNECTED"
                .storageUtilizationPercent(health.storageUtilizationPercent()) // 68.0%
                .cpuUsagePercent(health.cpuUsagePercent())
                .memoryUsagePercent(health.memoryUsagePercent())
                .lastUpdated(java.time.Instant.now())
                .build();

        // 3. Operational Statistics & Health (#7, #12)
        OperationalStatisticsResponse operational = new OperationalStatisticsResponse();
        operational.setCpuUtilization(health.cpuUsagePercent());
        operational.setMemoryUsage(health.memoryUsagePercent());
        operational.setStorageUtilization(health.storageUtilizationPercent());
        operational.setApiRequests(125400L);
        operational.setBackgroundJobs(48L);
        operational.setFailedJobs(1L);

        // 4. Security Overview & Statistics (#12)
        SecurityOverviewResponse security = new SecurityOverviewResponse();
        security.setFailedLoginAttempts(7L);
        security.setLockedAccounts(1L);
        security.setSecurityAlerts(2L);
        security.setActiveSessions(userStats.onlineUsers());
        security.setAuditEvents(580L);

        // 5. Administrator Activity Monitoring (#8)
        List<RecentActivityResponse> activities = List.of(
                createActivity(1L, 101L, "superadmin", "SUPER_ADMIN", "Updated Platform Branding Logo", "Platform Branding", "SUCCESS", 10),
                createActivity(2L, 101L, "superadmin", "SUPER_ADMIN", "Renewed Enterprise License #LIC-882", "License Management", "SUCCESS", 35),
                createActivity(3L, 102L, "system.admin", "ADMIN", "Enabled Feature 'TWO_FACTOR_AUTH'", "Feature Management", "SUCCESS", 75),
                createActivity(4L, 103L, "secops.lead", "SECURITY_ADMIN", "Investigated 3 Failed Logins", "Security Operations", "WARNING", 120)
        );

        // 6. Platform Notifications & Alerts (#11)
        DashboardAlertResponse alert1 = new DashboardAlertResponse(
                "ALT-101",
                "Storage Utilization Warning",
                "Platform storage utilization reached " + health.storageUtilizationPercent() + "%. Consider archiving logs.",
                "WARNING",
                "Platform Health",
                LocalDateTime.now().minusMinutes(20),
                "/api/v1/health"
        );

        DashboardAlertResponse alert2 = new DashboardAlertResponse(
                "ALT-102",
                "Licenses Expiring Soon",
                "3 software licenses will expire in the next 15 days.",
                "INFO",
                "License Management",
                LocalDateTime.now().minusHours(2),
                "/api/v1/licenses"
        );
        DashboardAlertResponse alert3 = new DashboardAlertResponse(       // <-- ADDED to match picture
                "ALT-103",
                "Organizations Awaiting Approval",
                "3 organizations awaiting activation approval.",
                "WARNING",
                "Organization Management",
                LocalDateTime.now().minusHours(1),
                "/api/v1/organizations"
        );
        List<DashboardAlertResponse> alerts = List.of(alert1, alert2,alert3);

        // 7. Navigation Directory to All Platform Administration Modules (#2, #3, #4, #5, #6, #10)
        List<ModuleNavigationResponse> modules = getAllModules();

        // 8. Software Licenses Summary (#5)
        Map<String, Object> licenseSummary = Map.of(
                "activeLicenses", licenseStats.activeLicenses(),
                "activeSubscriptions", licenseStats.activeSubscriptions(),
                "expiringWithin30Days", 27,
                "status", "ACTIVE",
                "managementEndpoint", "/api/v1/licenses"
        );

        // 9. Feature Management Summary (#6)
        Map<String, Object> featureSummary = Map.of(
                "totalFeatures", 18,
                "activeFeatures", 15,
                "betaFeatures", 3,
                "managementEndpoint", "/api/v1/features"
        );

        SuperAdminDashboardResponse response = new SuperAdminDashboardResponse();
        response.setPlatformSummary(summary);
        response.setSystemStatus(systemStatus);
        response.setOperationalStatistics(operational);
        response.setSecurityOverview(security);
        response.setRecentActivities(activities);
        response.setRecentLoginActivities(loginActivities);
        response.setNotificationsAndAlerts(alerts);
        response.setAdministrationModules(modules);
        response.setLicenseManagementSummary(licenseSummary);
        response.setFeatureManagementSummary(featureSummary);

        return response;
    }

    @Override
    public List<AdminFunctionSearchResponse> searchFunctions(String query) {
        String lowerQ = (query == null) ? "" : query.trim().toLowerCase();
        List<AdminFunctionSearchResponse> catalog = getFunctionCatalog();

        if (lowerQ.isEmpty()) {
            return catalog;
        }

        return catalog.stream()
                .filter(fn -> fn.getFunctionName().toLowerCase().contains(lowerQ)
                        || fn.getModule().toLowerCase().contains(lowerQ)
                        || fn.getDescription().toLowerCase().contains(lowerQ)
                        || fn.getKeywords().stream().anyMatch(k -> k.toLowerCase().contains(lowerQ)))
                .toList();
    }

    private List<ModuleNavigationResponse> getAllModules() {
        return List.of(
                new ModuleNavigationResponse("PLATFORM_CONFIG", "Platform Configuration", "Manage system-wide configuration keys, versions, and rollbacks", "System Settings", "/api/v1/platform-configurations", "/admin/configurations", "config-icon"),
                new ModuleNavigationResponse("USER_MGMT", "User Management", "Invite, roles and access control", "Administration", "/api/v1/users", "/admin/users", "user-icon"),
                new ModuleNavigationResponse("GLOBAL_SETTINGS", "Global Settings", "Configure platform properties, tenant defaults, and export CSV", "System Settings", "/api/v1/platform-settings", "/admin/settings", "settings-icon"),
                new ModuleNavigationResponse("BRANDING", "Platform Branding", "Manage logos, favicons, login backgrounds, and color themes", "Customization", "/api/v1/branding", "/admin/branding", "palette-icon"),
                new ModuleNavigationResponse("LICENSES", "Software License Management", "Create, assign, suspend, and renew software licenses", "Governance", "/api/v1/licenses", "/admin/licenses", "key-icon"),
                new ModuleNavigationResponse("FEATURES", "Feature Management", "Enable or disable platform features across tenants", "Governance", "/api/v1/features", "/admin/features", "toggle-icon"),
                new ModuleNavigationResponse("HEALTH", "Platform Health & Monitoring", "Inspect services, database connections, and operational health", "Operations", "/api/v1/health", "/admin/health", "heartbeat-icon")
        );
    }

    private List<AdminFunctionSearchResponse> getFunctionCatalog() {
        return List.of(
                new AdminFunctionSearchResponse("Platform Configuration", "Platform Configuration", "Manage system configurations and rollbacks", "/api/v1/platform-configurations", "/admin/configurations", List.of("config", "rollback", "restore", "keys")),
                new AdminFunctionSearchResponse("Global Settings", "Platform Settings", "Update global settings and export settings to CSV", "/api/v1/platform-settings", "/admin/settings", List.of("settings", "parameters", "export", "global")),
                new AdminFunctionSearchResponse("Platform Branding", "Branding", "Upload logos, favicons, backgrounds, and publish themes", "/api/v1/branding", "/admin/branding", List.of("branding", "logo", "favicon", "theme", "colors")),
                new AdminFunctionSearchResponse("License Management", "Licenses", "Manage licenses, renew, suspend, and assign to tenants", "/api/v1/licenses", "/admin/licenses", List.of("license", "subscription", "renew", "plan")),
                new AdminFunctionSearchResponse("Feature Flags & Toggles", "Features", "Activate or deactivate platform feature modules", "/api/v1/features", "/admin/features", List.of("feature", "enable", "disable", "toggle", "beta")),
                new AdminFunctionSearchResponse("System Health Monitoring", "Platform Health", "View live server metrics, memory, CPU, and database status", "/api/v1/health", "/admin/health", List.of("health", "cpu", "memory", "database", "uptime")),
                new AdminFunctionSearchResponse("Audit Logs & Activities", "Administration", "Monitor administrative operations and user sessions", "/api/v1/admin/dashboard", "/admin/activities", List.of("audit", "admin", "logs", "activity", "security"))
        );
    }

    private RecentActivityResponse createActivity(Long auditId, Long userId, String username, String role, String activity, String module, String status, int minutesAgo) {
        RecentActivityResponse item = new RecentActivityResponse();
        item.setAuditId(auditId);
        item.setUserId(userId);
        item.setUsername(username);
        item.setRole(role);
        item.setActivity(activity);
        item.setModule(module);
        item.setStatus(status);
        item.setDateTime(LocalDateTime.now().minusMinutes(minutesAgo));
        return item;
    }
}