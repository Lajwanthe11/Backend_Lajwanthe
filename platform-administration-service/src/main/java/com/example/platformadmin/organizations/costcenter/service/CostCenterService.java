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

    CostCenterResponseDTO getByCode(String code);

    List<CostCenterResponseDTO> getByOrganizationId(
            UUID organizationId
    );

    List<CostCenterResponseDTO> getByDepartmentId(
            Long departmentId
    );

    List<CostCenterResponseDTO> getByCompanyId(
            Long companyId
    );
}