package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostCenterRepository
        extends JpaRepository<CostCenterEntity, Long> {

    // Existing duplicate-code validation
    boolean existsByCostCenterCodeIgnoreCase(String costCenterCode);

    boolean existsByCostCenterCodeIgnoreCaseAndIdNot(
            String costCenterCode,
            Long id
    );

    Optional<CostCenterEntity> findByCostCenterCodeIgnoreCase(
            String costCenterCode
    );

    // Soft delete
    Optional<CostCenterEntity> findByIdAndIsDeletedFalse(Long id);

    List<CostCenterEntity> findByIsDeletedFalse();

    Page<CostCenterEntity> findByIsDeletedFalse(Pageable pageable);

    // Search by status
    List<CostCenterEntity> findByStatusAndIsDeletedFalse(
            CostCenterStatus status
    );

    // Search by organization ID
    List<CostCenterEntity> findByOrganization_IdAndIsDeletedFalse(
            UUID organizationId
    );

    // Search by company ID
    List<CostCenterEntity> findByCompany_IdAndIsDeletedFalse(
            Long companyId
    );

    // Search by department ID
    List<CostCenterEntity> findByDepartment_IdAndIsDeletedFalse(
            Long departmentId
    );
}