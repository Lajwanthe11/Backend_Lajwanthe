package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;

import com.example.platformadmin.organizations.company.exception.CompanyNotFoundException;
import com.example.platformadmin.organizations.company.repository.CompanyRepository;

import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.costcenter.exceptions.CostCenterAlreadyExistsException;
import com.example.platformadmin.organizations.costcenter.exceptions.CostCenterNotFoundException;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;

import com.example.platformadmin.organizations.department.exception.DepartmentNotFoundException;
import com.example.platformadmin.organizations.department.repository.DepartmentRepository;

import com.example.platformadmin.organizations.organization.exception.OrganizationNotFoundException;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CostCenterServiceImpl
        extends AbstractService<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO>
        implements CostCenterService {

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
    // DTO -> ENTITY
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

        entity.setBudgetAmount(
                dto.getBudgetAmount()
        );

        entity.setCurrency(
                dto.getCurrency() != null
                        && !dto.getCurrency().isBlank()
                        ? dto.getCurrency()
                        : "USD"
        );

        entity.setStatus(
                dto.getStatus() != null
                        ? dto.getStatus()
                        : CostCenterStatus.ACTIVE
        );

        return entity;
    }

    // =========================================================
    // ENTITY -> DTO
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

        dto.setBudgetAmount(
                entity.getBudgetAmount()
        );

        dto.setCurrency(
                entity.getCurrency()
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

        entity.setBudgetAmount(
                dto.getBudgetAmount()
        );

        if (dto.getCurrency() != null
                && !dto.getCurrency().isBlank()) {

            entity.setCurrency(
                    dto.getCurrency()
            );
        }

        if (dto.getStatus() != null) {
            entity.setStatus(
                    dto.getStatus()
            );
        }
    }

    // =========================================================
    // VALIDATE ORGANIZATION / COMPANY / DEPARTMENT
    // =========================================================

    private void validateReferences(
            CostCenterRequestDTO dto) {

        if (!organizationRepository.existsById(
                dto.getOrganizationId())) {

            throw new OrganizationNotFoundException(
                    dto.getOrganizationId()
            );
        }

        if (!companyRepository.existsById(
                dto.getCompanyId())) {

            throw new CompanyNotFoundException(
                    "Company not found with ID: "
                            + dto.getCompanyId()
            );
        }

        if (!departmentRepository.existsById(
                dto.getDepartmentId())) {

            throw new DepartmentNotFoundException(
                    dto.getDepartmentId()
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

        // Validate Organization
        // Validate Company
        // Validate Department
        validateReferences(dto);

        // Only Cost Center Code must be unique.
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

        // Validate Organization
        // Validate Company
        // Validate Department
        validateReferences(dto);

        // Only Cost Center Code must be unique.
        if (costCenterRepository
                .existsByCostCenterCodeAndIdNot(
                        dto.getCostCenterCode(),
                        entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists: "
                            + dto.getCostCenterCode()
            );
        }

        // NO duplicate name validation.
        //
        // Cost Center Name can be repeated.
        //
        // Organization ID can be repeated.
        // Company ID can be repeated.
        // Department ID can be repeated.
        // Budget can be repeated.
        // Currency can be repeated.
        // Status can be repeated.
    }

    // =========================================================
    // GET BY DEPARTMENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByDepartmentId(
            Long departmentId) {

        if (departmentId == null) {
            throw new IllegalArgumentException(
                    "Department ID cannot be null"
            );
        }

        if (!departmentRepository.existsById(
                departmentId)) {

            throw new DepartmentNotFoundException(
                    departmentId
            );
        }

        return costCenterRepository
                .findByDepartmentId(departmentId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // GET BY ORGANIZATION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByOrganizationId(
            UUID organizationId) {

        if (organizationId == null) {
            throw new IllegalArgumentException(
                    "Organization ID cannot be null"
            );
        }

        if (!organizationRepository.existsById(
                organizationId)) {

            throw new OrganizationNotFoundException(
                    organizationId
            );
        }

        return costCenterRepository
                .findByOrganizationId(organizationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // GET BY COMPANY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByCompanyId(
            Long companyId) {

        if (companyId == null) {
            throw new IllegalArgumentException(
                    "Company ID cannot be null"
            );
        }

        if (!companyRepository.existsById(companyId)) {

            throw new CompanyNotFoundException(
                    "Company not found with ID: "
                            + companyId
            );
        }

        return costCenterRepository
                .findByCompanyId(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // GET BY COST CENTER CODE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CostCenterResponseDTO getByCode(
            String costCenterCode) {

        if (costCenterCode == null
                || costCenterCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Cost center code cannot be blank"
            );
        }

        CostCenterEntity entity =
                costCenterRepository
                        .findByCostCenterCode(
                                costCenterCode
                        )
                        .orElseThrow(
                                () -> new CostCenterNotFoundException(
                                        "Cost center not found with code: "
                                                + costCenterCode
                                )
                        );

        return toDto(entity);
    }
}