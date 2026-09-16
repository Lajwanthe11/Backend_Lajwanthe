package com.example.platformadmin.superadmin.globaldashboard.controller;

import com.example.platformadmin.superadmin.globaldashboard.dto.response.*;
import com.example.platformadmin.superadmin.globaldashboard.dto.response.*;
import com.example.platformadmin.superadmin.globaldashboard.service.GlobalDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/v1/global-dashboard")
@Tag(
        name = "Global Dashboard",
        description = "Global platform dashboard APIs"
)
@SecurityRequirement(name = "basicAuth")
public class GlobalDashboardController {

    private final GlobalDashboardService globalDashboardService;

    public GlobalDashboardController(
            GlobalDashboardService globalDashboardService) {
        this.globalDashboardService = globalDashboardService;
    }

    @GetMapping
    @Operation(
            summary = "Get global dashboard",
            description = "Returns the complete global dashboard including summary, metrics and service status"
    )
    public GlobalDashboardResponse getDashboard() {
        return globalDashboardService.getDashboard();
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get dashboard summary",
            description = "Returns tenant, organization and user summary information"
    )
    public GlobalDashboardSummaryResponse getSummary() {
        return globalDashboardService.getSummary();
    }

    @GetMapping("/metrics")
    @Operation(
            summary = "Get platform metrics",
            description = "Returns platform utilization, API request, background job and license metrics"
    )
    public GlobalDashboardMetricsResponse getMetrics() {
        return globalDashboardService.getMetrics();
    }

    @GetMapping("/status")
    @Operation(
            summary = "Get service status",
            description = "Returns the overall platform service health status"
    )
    public GlobalDashboardStatusResponse getStatus() {
        return globalDashboardService.getStatus();
    }

    @GetMapping("/recent-activities")
    @Operation(
            summary = "Get recent activities",
            description = "Returns recent platform activities"
    )
    public List<RecentActivityResponse> getRecentActivities() {
        return globalDashboardService.getRecentActivities();
    }

    @GetMapping("/notifications")
    @Operation(
            summary = "Get dashboard notifications",
            description = "Returns global dashboard notifications"
    )
    public List<GlobalDashboardNotificationResponse> getNotifications() {
        return globalDashboardService.getNotifications();
    }
}