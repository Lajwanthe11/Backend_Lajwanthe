package com.example.platformadmin.superadmin.superadmindashboard.controller;


import com.example.platformadmin.superadmin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.example.platformadmin.superadmin.superadmindashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/v1/admin", "/api/v1/admin"}) // <--- Supports both URL formats
@Tag(name = "Super Admin Dashboard", description = "Consolidated Super Admin Dashboard API")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Super Admin Dashboard", description = "Returns overview, health, stats, alerts, activities, and module navigation")
    public ResponseEntity<SuperAdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

}