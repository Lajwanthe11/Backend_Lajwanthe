package com.example.platformadmin.superadmin.superadmindashboard.controller;

import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.platform.admin.superadmindashboard.service.DashboardService;

@RestController
@RequestMapping("/api/v1/admin")

public class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping("/dashboard")
    public SuperAdminDashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }
}
