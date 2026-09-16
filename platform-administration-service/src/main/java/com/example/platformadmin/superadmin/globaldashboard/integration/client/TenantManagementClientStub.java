package com.example.platformadmin.superadmin.globaldashboard.integration.client;

import com.example.platformadmin.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile({"local", "default"})
public class TenantManagementClientStub implements TenantManagementClient {

    @Override
    public TenantSummaryResponse getTenantSummary() {
        return new TenantSummaryResponse(
                10,
                8
        );
    }
}