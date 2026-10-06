package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.company.entity.Company;
import com.example.platformadmin.organizations.company.repository.CompanyRepository;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.costcenter.exception.*;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;
import com.example.platformadmin.organizations.department.entity.Department;
import com.example.platformadmin.organizations.department.repository.DepartmentRepository;
import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final OrganizationRepository organizationRepository;

    public CostCenterServiceImpl(
            CostCenterRepository costCenterRepository,
            DepartmentRepository departmentRepository,
            CompanyRepository companyRepository,
            OrganizationRepository organizationRepository) {

        super(costCenterRepository, "Cost Center");

        this.costCenterRepository = costCenterRepository;
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.organizationRepository = organizationRepository;
    }

    // -------------------------------------------------------
    // Request DTO -> Entity
    // -------------------------------------------------------

    @Override
    protected CostCenterEntity toEntity(CostCenterRequestDTO dto) {

        CostCenterEntity entity = new CostCenterEntity();

        entity.setCostCenterCode(dto.getCostCenterCode().trim());
        entity.setCostCenterName(dto.getCostCenterName().trim());

        entity.setDescription(
                dto.getDescription() != null
                        ? dto.getDescription().trim()
                        : null
        );

        entity.setStatus(dto.getStatus());

        entity.setBudget(dto.getBudget());
        entity.setAllocatedFunds(dto.getAllocatedFunds());
        entity.setDepartmentalExpenses(dto.getDepartmentalExpenses());

        return entity;
    }

    // -------------------------------------------------------
    // Entity -> Response DTO
    // -------------------------------------------------------

    @Override
    protected CostCenterResponseDTO toDto(CostCenterEntity entity) {

        CostCenterResponseDTO dto = new CostCenterResponseDTO();

        dto.setId(entity.getId());

        dto.setCostCenterCode(entity.getCostCenterCode());
        dto.setCostCenterName(entity.getCostCenterName());

        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());

        dto.setOrganizationId(
                entity.getOrganization() != null
                        ? entity.getOrganization().getId()
                        : null
        );

        dto.setCompanyId(
                entity.getCompany() != null
                        ? entity.getCompany().getId()
                        : null
        );

        dto.setDepartmentId(
                entity.getDepartment() != null
                        ? entity.getDepartment().getId()
                        : null
        );

        dto.setBudget(entity.getBudget());
        dto.setAllocatedFunds(entity.getAllocatedFunds());
        dto.setDepartmentalExpenses(entity.getDepartmentalExpenses());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    // -------------------------------------------------------
    // Update Existing Entity
    // -------------------------------------------------------

    @Override
    protected void updateEntityFromDto(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        entity.setCostCenterCode(dto.getCostCenterCode().trim());
        entity.setCostCenterName(dto.getCostCenterName().trim());

        entity.setDescription(
                dto.getDescription() != null
                        ? dto.getDescription().trim()
                        : null
        );

        entity.setStatus(dto.getStatus());

        entity.setBudget(dto.getBudget());
        entity.setAllocatedFunds(dto.getAllocatedFunds());
        entity.setDepartmentalExpenses(dto.getDepartmentalExpenses());
    }

    // -------------------------------------------------------
    // Before Create
    // -------------------------------------------------------

    @Override
    protected void beforeCreate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        ensureCodeAvailable(
                entity.getCostCenterCode(),
                null
        );

        applyRelationships(entity, dto);
    }

    // -------------------------------------------------------
    // Before Update
    // -------------------------------------------------------

    @Override
    protected void beforeUpdate(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        ensureCodeAvailable(
                entity.getCostCenterCode(),
                entity.getId()
        );

        applyRelationships(entity, dto);
    }

    // =======================================================
    // SOFT DELETE SUPPORT
    // =======================================================

    @Override
    @Transactional(readOnly = true)
    public CostCenterResponseDTO getById(Long id) {

        CostCenterEntity entity =
                getCostCenterOrThrow(id);

        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getAll() {

        return costCenterRepository
                .findByIsDeletedFalse()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CostCenterResponseDTO> getAll(Pageable pageable) {

        return costCenterRepository
                .findByIsDeletedFalse(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public CostCenterResponseDTO update(
            Long id,
            CostCenterRequestDTO dto) {

        CostCenterEntity entity =
                getCostCenterOrThrow(id);

        updateEntityFromDto(entity, dto);

        beforeUpdate(entity, dto);

        CostCenterEntity updatedEntity =
                costCenterRepository.save(entity);

        CostCenterResponseDTO response =
                toDto(updatedEntity);

        afterUpdate(updatedEntity, response);

        return response;
    }

    // -------------------------------------------------------
    // Soft Delete
    // -------------------------------------------------------

    @Override
    @Transactional
    public void deleteById(Long id) {

        CostCenterEntity entity =
                getCostCenterOrThrow(id);

        entity.setIsDeleted(true);
        entity.setDeletedAt(LocalDate.now());
        entity.setDeletedBy(resolveCurrentUser());

        costCenterRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {

        validateId(id);

        return costCenterRepository
                .findByIdAndIsDeletedFalse(id)
                .isPresent();
    }

    // =======================================================
    // SEARCH METHODS
    // =======================================================

    // Search by Status
    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByStatus(
            CostCenterStatus status) {

        return costCenterRepository
                .findByStatusAndIsDeletedFalse(status)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Search by Organization ID
    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByOrganizationId(
            UUID organizationId) {

        // Validate organization ID
        getOrganization(organizationId);

        return costCenterRepository
                .findByOrganization_IdAndIsDeletedFalse(
                        organizationId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Search by Company ID
    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByCompanyId(
            Long companyId) {

        // Validate company ID
        getCompany(companyId);

        return costCenterRepository
                .findByCompany_IdAndIsDeletedFalse(
                        companyId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Search by Department ID
    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponseDTO> getByDepartmentId(
            Long departmentId) {

        // Validate department ID
        getDepartment(departmentId);

        return costCenterRepository
                .findByDepartment_IdAndIsDeletedFalse(
                        departmentId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =======================================================
    // HELPER METHODS
    // =======================================================

    private CostCenterEntity getCostCenterOrThrow(Long id) {

        validateId(id);

        return costCenterRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(
                        () -> new CostCenterNotFoundException(
                                "Cost Center not found with id: " + id
                        )
                );
    }

    private void ensureCodeAvailable(
            String costCenterCode,
            Long currentId) {

        boolean exists;

        if (currentId == null) {

            exists =
                    costCenterRepository
                            .existsByCostCenterCodeIgnoreCase(
                                    costCenterCode
                            );

        } else {

            exists =
                    costCenterRepository
                            .existsByCostCenterCodeIgnoreCaseAndIdNot(
                                    costCenterCode,
                                    currentId
                            );
        }

        if (exists) {

            throw new CostCenterAlreadyExistsException(
                    "Cost Center code already exists: "
                            + costCenterCode
            );
        }
    }

    private void applyRelationships(
            CostCenterEntity entity,
            CostCenterRequestDTO dto) {

        entity.setOrganization(
                getOrganization(dto.getOrganizationId())
        );

        entity.setCompany(
                getCompany(dto.getCompanyId())
        );

        entity.setDepartment(
                getDepartment(dto.getDepartmentId())
        );
    }

    private OrganizationEntity getOrganization(
            UUID organizationId) {

        return organizationRepository
                .findById(organizationId)
                .orElseThrow(
                        () -> new InvalidOrganizationIdException(
                                organizationId
                        )
                );
    }

    private Company getCompany(Long companyId) {

        return companyRepository
                .findById(companyId)
                .orElseThrow(
                        () -> new InvalidCompanyIdException(
                                companyId
                        )
                );
    }

    private Department getDepartment(Long departmentId) {

        return departmentRepository
                .findById(departmentId)
                .orElseThrow(
                        () -> new InvalidDepartmentIdException(
                                departmentId
                        )
                );
    }

    // -------------------------------------------------------
    // Current Logged-in User
    // -------------------------------------------------------

    private String resolveCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()) {

            Object principal =
                    authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }

            if (authentication.getName() != null
                    && !authentication
                    .getName()
                    .equalsIgnoreCase("anonymousUser")) {

                return authentication.getName();
            }
        }

        return "SYSTEM";
    }
}