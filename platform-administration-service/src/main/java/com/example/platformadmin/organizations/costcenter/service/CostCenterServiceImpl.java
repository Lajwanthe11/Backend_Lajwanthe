package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.exceptions.CostCenterAlreadyExistsException;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;
import org.springframework.stereotype.Service;

@Service
public class CostCenterServiceImpl extends AbstractService<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO> implements CostCenterService {

    private final CostCenterRepository costCenterRepository;

    public CostCenterServiceImpl(
            CostCenterRepository costCenterRepository) {

        super(costCenterRepository, "Cost Center");
        this.costCenterRepository = costCenterRepository;
    }

    @Override
    protected CostCenterEntity toEntity(
            CostCenterRequestDTO dto) {

        CostCenterEntity entity = new CostCenterEntity();

        entity.setCostCenterCode(dto.getCostCenterCode());
        entity.setCostCenterName(dto.getCostCenterName());
        entity.setDescription(dto.getDescription());
        entity.setOrganizationId(dto.getOrganizationId());
        entity.setCompanyId(dto.getCompanyId());
        entity.setDepartmentId(dto.getDepartmentId());

        entity.setStatus(
                dto.getStatus() != null
                        ? dto.getStatus()
                        : com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus.ACTIVE
        );

        return entity;
    }

    @Override
    protected CostCenterResponseDTO toDto(
            CostCenterEntity entity) {

        CostCenterResponseDTO dto = new CostCenterResponseDTO();

        dto.setId(entity.getId());
        dto.setCostCenterCode(entity.getCostCenterCode());
        dto.setCostCenterName(entity.getCostCenterName());
        dto.setDescription(entity.getDescription());
        dto.setOrganizationId(entity.getOrganizationId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setStatus(entity.getStatus());

        dto.setTenantId(entity.getTenantId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setVersion(entity.getVersion());

        return dto;
    }

    @Override
    protected void updateEntityFromDto(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        entity.setCostCenterCode(dto.getCostCenterCode());
        entity.setCostCenterName(dto.getCostCenterName());
        entity.setDescription(dto.getDescription());
        entity.setOrganizationId(dto.getOrganizationId());
        entity.setCompanyId(dto.getCompanyId());
        entity.setDepartmentId(dto.getDepartmentId());

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    @Override
    protected void beforeCreate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        if (costCenterRepository.existsByCostCenterCode(
                dto.getCostCenterCode())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists: "
                            + dto.getCostCenterCode()
            );
        }
    }

    @Override
    protected void beforeUpdate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        if (costCenterRepository
                .existsByCostCenterCodeAndIdNot(
                        dto.getCostCenterCode(),
                        entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists: "
                            + dto.getCostCenterCode()
            );
        }
    }
}