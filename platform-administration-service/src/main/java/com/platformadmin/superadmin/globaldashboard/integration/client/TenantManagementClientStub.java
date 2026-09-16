package com.platformadmin.superadmin.globaldashboard.integration.client;

import com.platformadmin.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class TenantManagementClientStub implements TenantManagementClient {

    @Override
    public TenantSummaryResponse getTenantSummary() {
        return new TenantSummaryResponse(
                10,
                8
        );
    }
}