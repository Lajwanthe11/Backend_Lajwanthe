package com.example.platformadmin.superadmin.superadmindashboard.integration;

/**
 * Client for calling the License Management service (owned by another team
 * per Section 6 of the Sprint 1 plan - dependency only, no business logic
 * duplication). Real implementation should use RestClient + Eureka once
 * their API is ready. For now, see {@link LicenseManagementClientStub}.
 */
public interface LicenseManagementClient {

    LicenseStatistics getLicenseStatistics();

    record LicenseStatistics(long activeLicenses, long activeSubscriptions) {
    }
}