package com.platformadmin.superadmin.globaldashboard.integration.client;

import com.platformadmin.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;

public interface TenantManagementClient {

    TenantSummaryResponse getTenantSummary();
}