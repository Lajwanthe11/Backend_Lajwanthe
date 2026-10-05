package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;

import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.costcenter.exceptions.CostCenterAlreadyExistsException;
import com.example.platformadmin.organizations.costcenter.exceptions.CostCenterNotFoundException;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;

import com.example.platformadmin.organizations.organization.exception.OrganizationNotFoundException;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;

import com.example.platformadmin.organizations.company.exception.CompanyNotFoundException;
import com.example.platformadmin.organizations.company.repository.CompanyRepository;

import com.example.platformadmin.organizations.department.exception.DepartmentNotFoundException;
import com.example.platformadmin.organizations.department.repository.DepartmentRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CostCenterServiceImpl extends AbstractService<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO> implements CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final OrganizationRepository organizationRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;

    public CostCenterServiceImpl(
            CostCenterRepository costCenterRepository,
            OrganizationRepository organizationRepository,
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository) {

        super(costCenterRepository, "Cost Center");

        this.costCenterRepository = costCenterRepository;
        this.organizationRepository = organizationRepository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
    }

    // =========================================================
    // CREATE - Convert DTO to Entity
    // =========================================================

    @Override
    protected CostCenterEntity toEntity(
            CostCenterRequestDTO dto) {

        CostCenterEntity entity = new CostCenterEntity();

        entity.setCostCenterCode(
                dto.getCostCenterCode()
        );

        entity.setCostCenterName(
                dto.getCostCenterName()
        );

        entity.setDescription(
                dto.getDescription()
        );

        entity.setOrganizationId(
                dto.getOrganizationId()
        );

        entity.setCompanyId(
                dto.getCompanyId()
        );

        entity.setDepartmentId(
                dto.getDepartmentId()
        );

        entity.setStatus(
                dto.getStatus() != null
                        ? dto.getStatus()
                        : CostCenterStatus.ACTIVE
        );

        return entity;
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    @Override
    protected CostCenterResponseDTO toDto(
            CostCenterEntity entity) {

        CostCenterResponseDTO dto =
                new CostCenterResponseDTO();

        dto.setId(entity.getId());

        dto.setCostCenterCode(
                entity.getCostCenterCode()
        );

        dto.setCostCenterName(
                entity.getCostCenterName()
        );

        dto.setDescription(
                entity.getDescription()
        );

        dto.setOrganizationId(
                entity.getOrganizationId()
        );

        dto.setCompanyId(
                entity.getCompanyId()
        );

        dto.setDepartmentId(
                entity.getDepartmentId()
        );

        dto.setStatus(
                entity.getStatus()
        );

        dto.setTenantId(
                entity.getTenantId()
        );

        dto.setCreatedAt(
                entity.getCreatedAt()
        );

        dto.setUpdatedAt(
                entity.getUpdatedAt()
        );

        dto.setCreatedBy(
                entity.getCreatedBy()
        );

        dto.setUpdatedBy(
                entity.getUpdatedBy()
        );

        dto.setVersion(
                entity.getVersion()
        );

        return dto;
    }

    // =========================================================
    // UPDATE ENTITY
    // =========================================================

    @Override
    protected void updateEntityFromDto(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        entity.setCostCenterCode(
                dto.getCostCenterCode()
        );

        entity.setCostCenterName(
                dto.getCostCenterName()
        );

        entity.setDescription(
                dto.getDescription()
        );

        entity.setOrganizationId(
                dto.getOrganizationId()
        );

        entity.setCompanyId(
                dto.getCompanyId()
        );

        entity.setDepartmentId(
                dto.getDepartmentId()
        );

        if (dto.getStatus() != null) {

            entity.setStatus(
                    dto.getStatus()
            );
        }
    }

    // =========================================================
    // VALIDATE ORGANIZATION, COMPANY AND DEPARTMENT
    // =========================================================

    private void validateReferences(
            CostCenterRequestDTO dto) {

        // Organization must exist
        if (!organizationRepository.existsById(
                dto.getOrganizationId())) {

            throw new OrganizationNotFoundException(
                    dto.getOrganizationId()
            );
        }

        // Company must exist
        if (!companyRepository.existsById(
                dto.getCompanyId())) {

            throw new CompanyNotFoundException(
                    "Company not found with ID: "
                            + dto.getCompanyId()
            );
        }

        // Department must exist
        if (!departmentRepository.existsById(
                dto.getDepartmentId())) {

            throw new DepartmentNotFoundException(
                    "Department not found with ID: "
                            + dto.getDepartmentId()
            );
        }
    }

    // =========================================================
    // BEFORE CREATE
    // =========================================================

    @Override
    protected void beforeCreate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        // Validate referenced IDs
        validateReferences(dto);

        // ONLY COST CENTER CODE MUST BE UNIQUE
        if (costCenterRepository.existsByCostCenterCode(
                dto.getCostCenterCode())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists: "
                            + dto.getCostCenterCode()
            );
        }
    }

    // =========================================================
    // BEFORE UPDATE
    // =========================================================

    @Override
    protected void beforeUpdate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        // Validate referenced IDs
        validateReferences(dto);

        // ONLY COST CENTER CODE MUST BE UNIQUE
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

    // =========================================================
    // GET BY COST CENTER CODE
    // =========================================================

    @Override
    public CostCenterResponseDTO getByCode(
            String code) {

        CostCenterEntity entity =
                costCenterRepository
                        .findByCostCenterCode(code);

        if (entity == null) {

            throw new CostCenterNotFoundException(
                    "Cost center not found with code: "
                            + code
            );
        }

        return toDto(entity);
    }

    // =========================================================
    // GET BY ORGANIZATION ID
    // =========================================================

    @Override
    public List<CostCenterResponseDTO> getByOrganizationId(
            UUID organizationId) {

        return costCenterRepository
                .findByOrganizationId(organizationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // GET BY DEPARTMENT ID
    // =========================================================

    @Override
    public List<CostCenterResponseDTO> getByDepartmentId(
            Long departmentId) {

        return costCenterRepository
                .findByDepartmentId(departmentId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // GET BY COMPANY ID
    // =========================================================

    @Override
    public List<CostCenterResponseDTO> getByCompanyId(
            Long companyId) {

        return costCenterRepository
                .findByCompanyId(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }
}