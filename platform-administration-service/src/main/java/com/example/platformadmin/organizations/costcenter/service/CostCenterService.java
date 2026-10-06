package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.BaseService;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;

import java.util.List;
import java.util.UUID;

public interface CostCenterService extends BaseService<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO> {

    List<CostCenterResponseDTO> getByStatus(
            CostCenterStatus status
    );

    List<CostCenterResponseDTO> getByOrganizationId(
            UUID organizationId
    );

    List<CostCenterResponseDTO> getByCompanyId(
            Long companyId
    );

    List<CostCenterResponseDTO> getByDepartmentId(
            Long departmentId
    );
}