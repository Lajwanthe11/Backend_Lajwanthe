package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenterEntity, UUID> {

    Optional<CostCenterEntity> findByCostCenterCode(String costCenterCode);

    boolean existsByCostCenterCodeIgnoreCase(String costCenterCode);

    boolean existsByCostCenterCodeIgnoreCaseAndIdNot(String costCenterCode, UUID id);

    List<CostCenterEntity> findByOrganizationId(UUID organizationId);

    List<CostCenterEntity> findByDepartmentId(Long departmentId);

    List<CostCenterEntity> findByCompanyId(Long companyId);
}