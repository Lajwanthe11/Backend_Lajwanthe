package com.example.platformadmin.superadmin.globaldashboard.integration.client;

import com.example.platformadmin.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;

public interface TenantManagementClient {

    TenantSummaryResponse getTenantSummary();
}