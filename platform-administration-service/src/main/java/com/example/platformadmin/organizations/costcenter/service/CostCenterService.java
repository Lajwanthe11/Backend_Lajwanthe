package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.BaseService;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;

public interface CostCenterService extends BaseService<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO> {
}
