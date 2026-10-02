package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Cost Center data access.
 * Provides database operations for Cost Center entities,
 */

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenterEntity, Long> {

    boolean existsByCostCenterCodeIgnoreCase(String costCenterCode);

    boolean existsByCostCenterCodeIgnoreCaseAndIdNot(
            String costCenterCode,
            Long id
    );
}
