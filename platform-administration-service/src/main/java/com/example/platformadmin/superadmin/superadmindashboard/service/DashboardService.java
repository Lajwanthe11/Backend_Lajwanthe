package com.example.platformadmin.superadmin.superadmindashboard.service;

import com.example.platformadmin.superadmin.superadmindashboard.dto.response.AdminFunctionSearchResponse;
import com.example.platformadmin.superadmin.superadmindashboard.dto.response.SuperAdminDashboardResponse;

import java.util.List;

public interface DashboardService {
    SuperAdminDashboardResponse getDashboard();
    List<AdminFunctionSearchResponse> searchFunctions(String query);
}
