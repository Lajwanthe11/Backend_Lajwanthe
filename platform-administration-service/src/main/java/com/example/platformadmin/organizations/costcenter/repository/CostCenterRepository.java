package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CostCenterRepository
        extends JpaRepository<CostCenterEntity, Long> {

    // Cost Center Name
    boolean existsByCostCenterName(String costCenterName);

    boolean existsByCostCenterNameAndIdNot(
            String costCenterName,
            Long id
    );

    // Cost Center Code must be unique
    // within Organization + Company + Department
    boolean existsByCostCenterCodeAndOrganizationIdAndCompanyIdAndDepartmentId(
            String costCenterCode,
            UUID organizationId,
            Long companyId,
            Long departmentId
    );

    boolean existsByCostCenterCodeAndOrganizationIdAndCompanyIdAndDepartmentIdAndIdNot(
            String costCenterCode,
            UUID organizationId,
            Long companyId,
            Long departmentId,
            Long id
    );

    // Organization + Company + Department combination
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
}