package com.platformadmin.superadmin.globaldashboard.integration.client;

import com.platformadmin.superadmin.globaldashboard.integration.dto.LicenseSummaryResponse;

public interface LicenseManagementClient {

    LicenseSummaryResponse getLicenseSummary();
}