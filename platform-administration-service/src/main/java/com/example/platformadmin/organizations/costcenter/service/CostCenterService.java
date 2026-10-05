package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.BaseService;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;

import java.util.List;
import java.util.UUID;

public interface CostCenterService extends BaseService<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO> {

    /**
     * Returns all Cost Centers associated with a Department,
     * including their allocated budget and currency.
     */
    List<CostCenterResponseDTO> getByDepartmentId(Long departmentId);

    /**
     * Returns all Cost Centers associated with an Organization.
     */
    List<CostCenterResponseDTO> getByOrganizationId(UUID organizationId);

    /**
     * Returns all Cost Centers associated with a Company.
     */
    List<CostCenterResponseDTO> getByCompanyId(Long companyId);

    /**
     * Returns a Cost Center by its unique Cost Center Code.
     */
    CostCenterResponseDTO getByCode(String costCenterCode);
}