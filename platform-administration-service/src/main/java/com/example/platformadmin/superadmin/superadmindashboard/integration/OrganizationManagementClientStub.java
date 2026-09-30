package com.example.platformadmin.superadmin.superadmindashboard.integration;

import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganizationManagementClientStub implements OrganizationManagementClient {

    private final OrganizationRepository organizationRepository;

    public OrganizationManagementClientStub(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    public OrganizationStatistics getOrganizationStatistics() {
        List<OrganizationEntity> orgs = organizationRepository.findAll();

        long totalOrganizations = orgs.size();
        long activeOrganizations = orgs.stream()
                .filter(o -> "ACTIVE".equalsIgnoreCase(o.getStatus()))
                .count();

        return new OrganizationStatistics(totalOrganizations, activeOrganizations);
    }
}