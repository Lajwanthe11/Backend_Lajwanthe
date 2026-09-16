package com.example.platformadmin.superadmin.superadmindashboard.integration;

import org.springframework.stereotype.Component;

/**
 * TEMPORARY stub - returns mock data so the dashboard pipeline can be built
 * and tested before the License Management service is available.
 *
 * TODO: replace with a real RestClient-based implementation once confirmed.
 */
@Component("superAdminLicenseClientStub")
public class LicenseManagementClientStub implements LicenseManagementClient {

    @Override
    public LicenseStatistics getLicenseStatistics() {
        return new LicenseStatistics(46, 39);
    }
}