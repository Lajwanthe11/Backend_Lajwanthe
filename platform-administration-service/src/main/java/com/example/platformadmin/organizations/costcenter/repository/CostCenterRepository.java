package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenterEntity, Long> {


    boolean existsByCostCenterNameAndIdNot(String costCenterName, Long id);

    boolean existsByCostCenterCode(String costCenterCode);

    boolean existsByCostCenterCodeAndIdNot(String costCenterCode, Long id);

    boolean existsByOrganizationIdAndCompanyIdAndDepartmentId(
            UUID organizationId,
            Long companyId,
            Long departmentId
    );

    boolean existsByOrganizationIdAndCompanyIdAndDepartmentIdAndIdNot(
            UUID organizationId,
            Long companyId,
            Long departmentId,
            Long id
    );

    List<CostCenterEntity> findByOrganizationIdAndCompanyIdAndDepartmentId(
            UUID organizationId,
            Long companyId,
            Long departmentId
    );

    List<CostCenterEntity> findByDepartmentId(Long departmentId);
}
