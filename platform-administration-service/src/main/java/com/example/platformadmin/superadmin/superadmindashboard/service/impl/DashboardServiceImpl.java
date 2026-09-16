package com.example.platformadmin.superadmin.superadmindashboard.service.impl;

import com.example.platformadmin.superadmin.superadmindashboard.dto.response.SuperAdminDashboardResponse;

import com.example.platformadmin.superadmin.superadmindashboard.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Override
    public SuperAdminDashboardResponse getDashboard() {
        return new SuperAdminDashboardResponse(); }
}
