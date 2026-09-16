package com.example.platformadmin.superadmin.feature_management_service.integration;

import java.util.UUID;

public interface TenantServiceClient {

    /*
     * Cross-service contract placeholder.
     *
     * Final Tenant Service endpoint must be agreed
     * before implementing the remote call.
     */

    boolean tenantExists(UUID tenantId);

    boolean organizationBelongsToTenant(
            UUID tenantId,
            UUID organizationId
    );
}