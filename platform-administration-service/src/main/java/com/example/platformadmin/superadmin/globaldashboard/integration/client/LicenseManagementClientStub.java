package com.example.platformadmin.superadmin.globaldashboard.integration.client;

import com.example.platformadmin.superadmin.globaldashboard.integration.dto.LicenseSummaryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("globalDashboardLicenseClientStub")
@Profile({"local", "default"})
public class LicenseManagementClientStub
        implements LicenseManagementClient {

    @Override
    public LicenseSummaryResponse getLicenseSummary() {
        return new LicenseSummaryResponse(142);
    }
}