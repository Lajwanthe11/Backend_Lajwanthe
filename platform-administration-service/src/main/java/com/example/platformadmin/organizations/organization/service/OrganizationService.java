package com.example.platformadmin.organizations.organization.service;

import com.example.common.abstracts.BaseService;
import com.example.platformadmin.organizations.organization.dto.OrganizationRequestDTO;
import com.example.platformadmin.organizations.organization.dto.OrganizationResponseDTO;
import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import com.example.platformadmin.organizations.organization.enums.OrganizationType;

import java.util.List;
import java.util.UUID;

public interface OrganizationService extends BaseService<
        OrganizationEntity,
        UUID,
        OrganizationRequestDTO,
        OrganizationResponseDTO> {

    OrganizationResponseDTO getByCode(String organizationCode);

    List<OrganizationResponseDTO> searchOrganizations(String query);

    List<OrganizationResponseDTO> getByType(OrganizationType organizationType);
}