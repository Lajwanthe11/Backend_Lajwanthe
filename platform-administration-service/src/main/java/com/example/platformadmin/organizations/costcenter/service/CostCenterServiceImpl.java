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
    // DTO -> ENTITY
    // =========================================================

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

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

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

        // BaseEntity fields
        dto.setTenantId(entity.getTenantId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setVersion(entity.getVersion());

        return dto;
    }

    // =========================================================
    // UPDATE ENTITY
    // =========================================================

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

    // =========================================================
    // VALIDATE REFERENCES
    // =========================================================

    private void validateReferences(CostCenterRequestDTO dto) {

        if (dto.getOrganizationId() == null) {
            throw new IllegalArgumentException(
                    "Organization ID cannot be null"
            );
        }

        if (!organizationRepository.existsById(dto.getOrganizationId())) {
            throw new OrganizationNotFoundException(
                    dto.getOrganizationId()
            );
        }

        if (dto.getCompanyId() == null) {
            throw new IllegalArgumentException(
                    "Company ID cannot be null"
            );
        }

        if (!companyRepository.existsById(dto.getCompanyId())) {
            throw new CompanyNotFoundException(
                    "Company not found with ID: "
                            + dto.getCompanyId()
            );
        }

        if (dto.getDepartmentId() == null) {
            throw new IllegalArgumentException(
                    "Department ID cannot be null"
            );
        }

        if (!departmentRepository.existsById(dto.getDepartmentId())) {
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

        validateReferences(dto);

        if (dto.getCostCenterCode() == null
                || dto.getCostCenterCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Cost center code cannot be null or empty"
            );
        }

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

        validateReferences(dto);

        if (costCenterRepository
                .existsByCostCenterCodeAndIdNot(
                        dto.getCostCenterCode(),
                        entity.getId())) {

            throw new CostCenterAlreadyExistsException(
                    "Cost center code already exists: "
                            + dto.getCostCenterCode()
            );
        }

//     if (costCenterRepository
//             .existsByCostCenterNameAndIdNot(
//                        dto.getCostCenterName(),
//                        entity.getId())) {
//
//            throw new CostCenterAlreadyExistsException(
//                    "Cost center name already exists: "
//                            + dto.getCostCenterName()
//            );
//        }
    }

    // =========================================================
    // GET BY DEPARTMENT ID
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

    // =========================================================
    // GET BY COMPANY ID
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
    // GET BY ORGANIZATION ID
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

        if (!organizationRepository.existsById(organizationId)) {
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
    // GET BY COST CENTER CODE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CostCenterResponseDTO getByCode(
            String code) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Cost center code cannot be null or empty"
            );
        }

        CostCenterEntity entity =
                costCenterRepository.findByCostCenterCode(code);

        if (entity == null) {
            throw new RuntimeException(
                    "Cost Center not found with code: "
                            + code
            );
        }

        return toDto(entity);
    }

    // =========================================================
    // GET BY COST CENTER ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CostCenterResponseDTO getByCostCenterId(
            Long costCenterId) {

        if (costCenterId == null) {
            throw new IllegalArgumentException(
                    "Cost Center ID cannot be null"
            );
        }

        CostCenterEntity entity =
                costCenterRepository
                        .findById(costCenterId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cost Center not found with ID: "
                                                + costCenterId
                                )
                        );

        return toDto(entity);
    }
}