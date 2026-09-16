package com.example.platformadmin.superadmin.superadmindashboard.service.impl;

import org.springframework.stereotype.Service;

import com.example.platformadmin.superadmin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.example.platformadmin.superadmin.superadmindashboard.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Override
    public SuperAdminDashboardResponse getDashboard() {
        return new SuperAdminDashboardResponse(); }
}
