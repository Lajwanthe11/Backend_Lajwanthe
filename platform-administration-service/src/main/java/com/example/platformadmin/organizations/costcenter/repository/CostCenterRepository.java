package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostCenterRepository
        extends JpaRepository<CostCenterEntity, Long> {

    // =========================================================
    // UNIQUE COST CENTER CODE
    // =========================================================

    boolean existsByCostCenterCode(String costCenterCode);

    boolean existsByCostCenterCodeAndIdNot(
            String costCenterCode,
            Long id
    );

    Optional<CostCenterEntity> findByCostCenterCode(
            String costCenterCode
    );

    // =========================================================
    // GET BY ORGANIZATION
    // =========================================================

    List<CostCenterEntity> findByOrganizationId(
            UUID organizationId
    );

    // =========================================================
    // GET BY COMPANY
    // =========================================================

    List<CostCenterEntity> findByCompanyId(
            Long companyId
    );

    // =========================================================
    // GET BY DEPARTMENT
    // =========================================================

    List<CostCenterEntity> findByDepartmentId(
            Long departmentId
    );
}