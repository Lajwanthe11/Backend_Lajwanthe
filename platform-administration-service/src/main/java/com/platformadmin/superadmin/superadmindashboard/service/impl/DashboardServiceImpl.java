package com.example.platformadmin.superadmin.superadmindashboard.service.impl;

import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.enterprise.platform.admin.superadmindashboard.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Override
    public SuperAdminDashboardResponse getDashboard() {
        return new SuperAdminDashboardResponse(); }
}
