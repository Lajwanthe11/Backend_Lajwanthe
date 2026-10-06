package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.company.exception.CompanyNotFoundException;
import com.example.platformadmin.organizations.company.repository.CompanyRepository;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.costcenter.exceptions.CostCenterAlreadyExistsException;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;
import com.example.platformadmin.organizations.department.exception.DepartmentNotFoundException;
import com.example.platformadmin.organizations.department.repository.DepartmentRepository;
import com.example.platformadmin.organizations.organization.exception.OrganizationNotFoundException;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    protected CostCenterEntity toEntity(CostCenterRequestDTO dto) {

        CostCenterEntity entity = new CostCenterEntity();

        entity.setCostCenterCode(dto.getCostCenterCode());
        entity.setCostCenterName(dto.getCostCenterName());
        entity.setDescription(dto.getDescription());
        entity.setOrganizationId(dto.getOrganizationId());
        entity.setCompanyId(dto.getCompanyId());
        entity.setDepartmentId(dto.getDepartmentId());
        entity.setBudgetAmount(dto.getBudgetAmount());

        entity.setStatus(
                dto.getStatus() != null
                        ? dto.getStatus()
                        : CostCenterStatus.ACTIVE
        );

        return entity;
    }

    @Override
    protected CostCenterResponseDTO toDto(CostCenterEntity entity) {

        CostCenterResponseDTO dto = new CostCenterResponseDTO();

        dto.setId(entity.getId());
        dto.setCostCenterCode(entity.getCostCenterCode());
        dto.setCostCenterName(entity.getCostCenterName());
        dto.setDescription(entity.getDescription());
        dto.setOrganizationId(entity.getOrganizationId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setBudgetAmount(entity.getBudgetAmount());
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
        entity.setBudgetAmount(dto.getBudgetAmount());

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    /**
     * Validates Organization, Company and Department references.
     */
    private void validateReferences(CostCenterRequestDTO dto) {

        if (!organizationRepository.existsById(dto.getOrganizationId())) {
            throw new OrganizationNotFoundException(
                    dto.getOrganizationId()
            );
        }

        if (!companyRepository.existsById(dto.getCompanyId())) {
            throw new CompanyNotFoundException(
                    "Company not found with ID: " + dto.getCompanyId()
            );
        }

        if (!departmentRepository.existsById(dto.getDepartmentId())) {
            throw new DepartmentNotFoundException(
                    dto.getDepartmentId()
            );
        }
    }

    /**
     * Validations executed before creating a Cost Center.
     */
    @Override
    protected void beforeCreate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        // Validate Organization, Company and Department
        validateReferences(dto);

        // Check duplicate Cost Center Code
        if (costCenterRepository.existsByCostCenterCode(
                dto.getCostCenterCode())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists: "
                            + dto.getCostCenterCode()
            );
        }


    }

    /**
     * Validations executed before updating a Cost Center.
     */
    @Override
    protected void beforeUpdate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        // Validate Organization, Company and Department
        validateReferences(dto);

        // Check duplicate Cost Center Code
        // Exclude the current Cost Center ID.
        if (costCenterRepository.existsByCostCenterCodeAndIdNot(
                dto.getCostCenterCode(),
                entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists: "
                            + dto.getCostCenterCode()
            );
        }

        // Check duplicate Cost Center Name
        // Exclude the current Cost Center ID.
        if (costCenterRepository.existsByCostCenterNameAndIdNot(
                dto.getCostCenterName(),
                entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center name already exists: "
                            + dto.getCostCenterName()
            );
        }
    }

    /**
     * Returns all Cost Centers associated with a Department.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByDepartmentId(
            Long departmentId) {

        if (departmentId == null) {
            throw new IllegalArgumentException(
                    "Department ID cannot be null"
            );
        }

        if (!departmentRepository.existsById(departmentId)) {
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
}