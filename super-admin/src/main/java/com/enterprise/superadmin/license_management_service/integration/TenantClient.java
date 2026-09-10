package com.enterprise.superadmin.license_management_service.integration;

import com.enterprise.superadmin.license_management_service.integration.dto.TenantResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;
@Component
public class TenantClient {
    private final RestClient restClient;
    private final String tenantServiceUrl;
    public TenantClient(
            RestClient restClient,
            @Value("${integration.tenant-service-url:http://localhost:8082}")
            String tenantServiceUrl
    ) {
        this.restClient = restClient;
        this.tenantServiceUrl = tenantServiceUrl;
    }
    public TenantResponse getTenant(UUID tenantId) {
        return restClient.get()
                .uri(
                        tenantServiceUrl +
                                "/api/v1/tenants/{id}",
                        tenantId
                )
                .retrieve()
                .body(TenantResponse.class);
    }
}

