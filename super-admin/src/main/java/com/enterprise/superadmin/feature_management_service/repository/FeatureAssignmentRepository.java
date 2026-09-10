package com.enterprise.superadmin.feature_management_service.repository;


import com.enterprise.superadmin.feature_management_service.entity.FeatureAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureAssignmentRepository
        extends JpaRepository<FeatureAssignment, UUID> {

    List<FeatureAssignment> findByFeatureId(UUID featureId);

    List<FeatureAssignment> findByTenantId(UUID tenantId);

    List<FeatureAssignment> findByOrganizationId(UUID organizationId);

    List<FeatureAssignment> findByLicensePlan(String licensePlan);

    List<FeatureAssignment> findByStatus(String status);

    Optional<FeatureAssignment> findByFeatureIdAndTenantId(
            UUID featureId,
            UUID tenantId
    );

    boolean existsByFeatureIdAndTenantId(
            UUID featureId,
            UUID tenantId
    );
}