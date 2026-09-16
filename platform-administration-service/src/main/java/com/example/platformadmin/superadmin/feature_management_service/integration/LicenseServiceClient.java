package com.example.platformadmin.superadmin.feature_management_service.integration;

import java.util.UUID;

public interface LicenseServiceClient {

    /*
     * Cross-service contract placeholder.
     *
     * Final endpoint and response contract must be agreed
     * with Team Panthers before implementation.
     *
     * Example future responsibility:
     *
     * Check whether a tenant has an active license
     * that permits a particular plan/feature.
     */

    boolean isLicenseEligible(
            UUID tenantId,
            String licensePlan
    );
}
