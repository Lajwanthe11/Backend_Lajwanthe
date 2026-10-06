package com.example.platformadmin.organizations.costcenter.Repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Cost Center data access.
 * Provides database operations for Cost Center entities,
 * including code uniqueness checks and filtering by
 * Organization, Company, Department, and status.
 */
@Repository
public interface CostCenterRepository
        extends JpaRepository<CostCenterEntity, Long> {

    boolean existsByCostCenterCodeIgnoreCase(String costCenterCode);

    boolean existsByCostCenterCodeIgnoreCaseAndIdNot(
            String costCenterCode,
            Long id
    );

    List<CostCenterEntity> findByOrganization_Id(
            UUID organizationId
    );

    List<CostCenterEntity> findByCompany_Id(
            Long companyId
    );

    List<CostCenterEntity> findByDepartment_Id(
            Long departmentId
    );

    List<CostCenterEntity> findByStatus(
            CostCenterStatus status
    );

}