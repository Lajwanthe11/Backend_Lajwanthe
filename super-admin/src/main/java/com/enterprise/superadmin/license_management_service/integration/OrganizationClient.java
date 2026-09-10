package com.enterprise.superadmin.license_management_service.integration;

import com.enterprise.superadmin.license_management_service.integration.dto.OrganizationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class OrganizationClient {

    private final RestClient restClient;
    private final String organizationServiceUrl;

    public OrganizationClient(
            RestClient restClient,
            @Value("${integration.organization-service-url:http://localhost:8081}")
            String organizationServiceUrl
    ) {
        this.restClient = restClient;
        this.organizationServiceUrl = organizationServiceUrl;
    }

    public OrganizationResponse getOrganization(UUID organizationId) {

        return restClient.get()
                .uri(
                        organizationServiceUrl +
                                "/api/v1/organizations/{id}",
                        organizationId
                )
                .retrieve()
                .body(OrganizationResponse.class);
    }
}
