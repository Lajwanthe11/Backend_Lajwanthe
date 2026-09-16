package com.example.platformadmin.superadmin.superadmindashboard.integration;

public interface OrganizationManagementClient {
    OrganizationStatistics getOrganizationStatistics();

    record OrganizationStatistics(long totalOrganizations, long activeAdministrators) {
    }
}
