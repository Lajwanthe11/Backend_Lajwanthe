package com.example.platformadmin.organizations.costcenter.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.company.entity.Company;
import com.example.platformadmin.organizations.company.repository.CompanyRepository;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.exception.CostCenterAlreadyExistsException;
import com.example.platformadmin.organizations.costcenter.exception.CostCenterNotFoundException;
import com.example.platformadmin.organizations.costcenter.exception.InvalidCompanyIdException;
import com.example.platformadmin.organizations.costcenter.exception.InvalidDepartmentIdException;
import com.example.platformadmin.organizations.costcenter.exception.InvalidOrganizationIdException;
import com.example.platformadmin.organizations.costcenter.repository.CostCenterRepository;
import com.example.platformadmin.organizations.department.entity.Department;
import com.example.platformadmin.organizations.department.repository.DepartmentRepository;
import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(readOnly = true)
    public CostCenterResponseDTO getById(Long id) {

        CostCenterEntity entity =
                getCostCenterOrThrow(id);

        return toDto(entity);
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

    @Override
    @Transactional
    public void deleteById(Long id) {

        CostCenterEntity entity =
                getCostCenterOrThrow(id);

        beforeDelete(id);

        costCenterRepository.delete(entity);

        afterDelete(id);
    }

    private CostCenterEntity getCostCenterOrThrow(Long id) {

        validateId(id);

        return costCenterRepository
                .findById(id)
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
}