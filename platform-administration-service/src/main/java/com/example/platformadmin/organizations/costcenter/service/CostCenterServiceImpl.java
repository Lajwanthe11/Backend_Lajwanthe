package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.company.exception.CompanyNotFoundException;
import com.example.platformadmin.organizations.company.repository.CompanyRepository;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.costcenter.exception.CostCenterAlreadyExistsException;
import com.example.platformadmin.organizations.costcenter.exception.CostCenterNotFoundException;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;
import com.example.platformadmin.organizations.department.exception.DepartmentNotFoundException;
import com.example.platformadmin.organizations.department.repository.DepartmentRepository;
import com.example.platformadmin.organizations.organization.exception.OrganizationNotFoundException;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CostCenterServiceImpl extends AbstractService<
        CostCenterEntity,
        UUID,
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
        super(costCenterRepository, "CostCenter");
        this.costCenterRepository = costCenterRepository;
        this.organizationRepository = organizationRepository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    protected void beforeCreate(CostCenterEntity entity, CostCenterRequestDTO dto) {
        // 1. Check unique code
        if (costCenterRepository.existsByCostCenterCodeIgnoreCase(dto.getCostCenterCode())) {
            throw new CostCenterAlreadyExistsException(
                    String.format("Cost Center with code '%s' already exists", dto.getCostCenterCode()));
        }

        // 2. Validate Parent Organization exists
        if (dto.getOrganizationId() == null || !organizationRepository.existsById(dto.getOrganizationId())) {
            throw new OrganizationNotFoundException(
                    String.format("Organization not found with ID: %s", dto.getOrganizationId()));
        }

        // 3. Validate Company exists (if provided)
        if (dto.getCompanyId() != null && !companyRepository.existsById(dto.getCompanyId())) {
            throw new CompanyNotFoundException(
                    String.format("Company not found with ID: %d", dto.getCompanyId()));
        }

        // 4. Validate Department exists (if provided)
        if (dto.getDepartmentId() != null && !departmentRepository.existsById(dto.getDepartmentId())) {
            throw new DepartmentNotFoundException(
                    String.format("Department not found with ID: %d", dto.getDepartmentId()));
        }
    }

    @Override
    protected void beforeUpdate(CostCenterEntity entity, CostCenterRequestDTO dto) {
        // 1. Check duplicate code for another entity
        if (costCenterRepository.existsByCostCenterCodeIgnoreCaseAndIdNot(dto.getCostCenterCode(), entity.getId())) {
            throw new CostCenterAlreadyExistsException(
                    String.format("Another Cost Center with code '%s' already exists", dto.getCostCenterCode()));
        }

        // 2. Validate Parent Organization exists
        if (dto.getOrganizationId() == null || !organizationRepository.existsById(dto.getOrganizationId())) {
            throw new OrganizationNotFoundException(
                    String.format("Organization not found with ID: %s", dto.getOrganizationId()));
        }

        // 3. Validate Company exists (if provided)
        if (dto.getCompanyId() != null && !companyRepository.existsById(dto.getCompanyId())) {
            throw new CompanyNotFoundException(
                    String.format("Company not found with ID: %d", dto.getCompanyId()));
        }

        // 4. Validate Department exists (if provided)
        if (dto.getDepartmentId() != null && !departmentRepository.existsById(dto.getDepartmentId())) {
            throw new DepartmentNotFoundException(
                    String.format("Department not found with ID: %d", dto.getDepartmentId()));
        }
    }

    @Override
    protected CostCenterEntity toEntity(CostCenterRequestDTO dto) {
        return CostCenterEntity.builder()
                .costCenterCode(dto.getCostCenterCode().trim().toUpperCase())
                .costCenterName(dto.getCostCenterName().trim())
                .description(dto.getDescription())
                .organizationId(dto.getOrganizationId())
                .companyId(dto.getCompanyId())
                .departmentId(dto.getDepartmentId())
                .allocatedBudget(dto.getAllocatedBudget())
                .currency(dto.getCurrency() != null ? dto.getCurrency().trim().toUpperCase() : "USD")
                .status(dto.getStatus() != null ? dto.getStatus() : CostCenterStatus.ACTIVE)
                .build();
    }

    @Override
    protected CostCenterResponseDTO toDto(CostCenterEntity entity) {
        if (entity == null) {
            return null;
        }
        return CostCenterResponseDTO.builder()
                .id(entity.getId())
                .costCenterCode(entity.getCostCenterCode())
                .costCenterName(entity.getCostCenterName())
                .description(entity.getDescription())
                .organizationId(entity.getOrganizationId())
                .companyId(entity.getCompanyId())
                .departmentId(entity.getDepartmentId())
                .allocatedBudget(entity.getAllocatedBudget())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    protected void updateEntityFromDto(CostCenterEntity entity, CostCenterRequestDTO dto) {
        entity.setCostCenterCode(dto.getCostCenterCode().trim().toUpperCase());
        entity.setCostCenterName(dto.getCostCenterName().trim());
        entity.setDescription(dto.getDescription());
        entity.setOrganizationId(dto.getOrganizationId());
        entity.setCompanyId(dto.getCompanyId());
        entity.setDepartmentId(dto.getDepartmentId());
        entity.setAllocatedBudget(dto.getAllocatedBudget());
        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            entity.setCurrency(dto.getCurrency().trim().toUpperCase());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public CostCenterResponseDTO getByCode(String code) {
        return costCenterRepository.findByCostCenterCode(code.trim().toUpperCase())
                .map(this::toDto)
                .orElseThrow(() -> new CostCenterNotFoundException(
                        String.format("Cost Center not found with code: '%s'", code)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByOrganizationId(UUID organizationId) {
        return costCenterRepository.findByOrganizationId(organizationId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByCompanyId(Long companyId) {
        return costCenterRepository.findByCompanyId(companyId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByDepartmentId(Long departmentId) {
        return costCenterRepository.findByDepartmentId(departmentId).stream()
                .map(this::toDto)
                .toList();
    }
}