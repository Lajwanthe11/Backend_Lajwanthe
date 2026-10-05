package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;

import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.costcenter.exceptions.CostCenterAlreadyExistsException;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;

import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;
import com.example.platformadmin.organizations.organization.exception.OrganizationNotFoundException;

import com.example.platformadmin.organizations.company.repository.CompanyRepository;
import com.example.platformadmin.organizations.company.exception.CompanyNotFoundException;

import com.example.platformadmin.organizations.department.repository.DepartmentRepository;
import com.example.platformadmin.organizations.department.exception.DepartmentNotFoundException;

import org.springframework.stereotype.Service;

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
                        : CostCenterStatus.ACTIVE
        );

        return entity;
    }

    @Override
    protected CostCenterResponseDTO toDto(
            CostCenterEntity entity) {

        CostCenterResponseDTO dto =
                new CostCenterResponseDTO();

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

    private void validateReferences(
            CostCenterRequestDTO dto) {

        // Organization validation
        if (!organizationRepository.existsById(
                dto.getOrganizationId())) {

            throw new OrganizationNotFoundException(
                    dto.getOrganizationId()
            );
        }

        // Company validation
        if (!companyRepository.existsById(
                dto.getCompanyId())) {

            throw new CompanyNotFoundException(
                    "Company not found with ID: "
                            + dto.getCompanyId()
            );
        }

        // Department validation
        if (!departmentRepository.existsById(
                dto.getDepartmentId())) {

            throw new DepartmentNotFoundException(
                    "Department not found with ID: "
                            + dto.getDepartmentId()
            );
        }
    }

    @Override
    protected void beforeCreate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        // Validate Organization, Company and Department
        validateReferences(dto);

        // Cost Center Code must be unique
        // for the same Organization + Company + Department
        if (costCenterRepository
                .existsByCostCenterCodeAndOrganizationIdAndCompanyIdAndDepartmentId(
                        dto.getCostCenterCode(),
                        dto.getOrganizationId(),
                        dto.getCompanyId(),
                        dto.getDepartmentId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists for the given "
                            + "organization, company and department: "
                            + dto.getCostCenterCode()
            );
        }

        // Cost Center Name must be unique
        if (costCenterRepository.existsByCostCenterName(
                dto.getCostCenterName())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center name already exists: "
                            + dto.getCostCenterName()
            );
        }

        // Same Organization + Company + Department
        // should not have another Cost Center
        if (costCenterRepository
                .existsByOrganizationIdAndCompanyIdAndDepartmentId(
                        dto.getOrganizationId(),
                        dto.getCompanyId(),
                        dto.getDepartmentId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center already exists for the given "
                            + "organization, company and department"
            );
        }
    }

    @Override
    protected void beforeUpdate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        // Validate Organization, Company and Department
        validateReferences(dto);

        // Cost Center Code must be unique
        // for the same Organization + Company + Department
        // Exclude current record
        if (costCenterRepository
                .existsByCostCenterCodeAndOrganizationIdAndCompanyIdAndDepartmentIdAndIdNot(
                        dto.getCostCenterCode(),
                        dto.getOrganizationId(),
                        dto.getCompanyId(),
                        dto.getDepartmentId(),
                        entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists for the given "
                            + "organization, company and department: "
                            + dto.getCostCenterCode()
            );
        }

        // Cost Center Name must be unique
        // Exclude current record
        if (costCenterRepository
                .existsByCostCenterNameAndIdNot(
                        dto.getCostCenterName(),
                        entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center name already exists: "
                            + dto.getCostCenterName()
            );
        }

        // Same Organization + Company + Department
        // should not have another Cost Center
        // Exclude current record
        if (costCenterRepository
                .existsByOrganizationIdAndCompanyIdAndDepartmentIdAndIdNot(
                        dto.getOrganizationId(),
                        dto.getCompanyId(),
                        dto.getDepartmentId(),
                        entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center already exists for the given "
                            + "organization, company and department"
            );
        }
    }
}