package com.example.platformadmin.superadmin.globaldashboard.integration.client;

import com.example.platformadmin.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("!local")
public class TenantManagementClientImpl implements TenantManagementClient {

    private final RestClient restClient;

    public TenantManagementClientImpl(
            RestClient.Builder restClientBuilder,
            @Value("${services.tenant.base-url}") String tenantBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(tenantBaseUrl)
                .build();
    }

    @Override
    public TenantSummaryResponse getTenantSummary() {
        return restClient.get()
                .uri("/api/v1/tenants/summary")
                .retrieve()
                .body(TenantSummaryResponse.class);
    }
}