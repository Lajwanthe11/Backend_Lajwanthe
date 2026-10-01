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

    boolean existsByCostCenterCodeIgnoreCase(String costCenterCode);

    boolean existsByCostCenterCodeIgnoreCaseAndIdNot(
            String costCenterCode,
            Long id
    );

    Optional<CostCenterEntity> findByCostCenterCodeIgnoreCase(
            String costCenterCode
    );

    List<CostCenterEntity> findByOrganization_Id(UUID organizationId);

    List<CostCenterEntity> findByCompany_Id(Long companyId);

    List<CostCenterEntity> findByDepartment_Id(Long departmentId);
}
