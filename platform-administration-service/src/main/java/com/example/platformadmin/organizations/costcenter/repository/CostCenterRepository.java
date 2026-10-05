package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostCenterRepository
        extends JpaRepository<CostCenterEntity, Long> {

    // ---------------------------------------------------------
    // Cost Center Code uniqueness
    // ---------------------------------------------------------

    boolean existsByCostCenterCode(
            String costCenterCode
    );

    boolean existsByCostCenterCodeAndIdNot(
            String costCenterCode,
            Long id
    );

    // ---------------------------------------------------------
    // Get by Cost Center Code
    // ---------------------------------------------------------

    CostCenterEntity findByCostCenterCode(
            String costCenterCode
    );

    // ---------------------------------------------------------
    // Get by Organization ID
    // ---------------------------------------------------------

    List<CostCenterEntity> findByOrganizationId(
            UUID organizationId
    );

    // ---------------------------------------------------------
    // Get by Department ID
    // ---------------------------------------------------------

    List<CostCenterEntity> findByDepartmentId(
            Long departmentId
    );

    // ---------------------------------------------------------
    // Get by Company ID
    // ---------------------------------------------------------

    List<CostCenterEntity> findByCompanyId(
            Long companyId
    );
}