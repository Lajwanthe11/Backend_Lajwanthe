package com.example.platformadmin.superadmin.feature_management_service.repository;



import com.example.platformadmin.superadmin.feature_management_service.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, UUID>, JpaSpecificationExecutor<Feature> {

    Optional<Feature> findByFeatureName(String featureName);

    List<Feature> findByModule(String module);

    List<Feature> findByStatus(String status);

    List<Feature> findByLicensePlan(String licensePlan);

    boolean existsByFeatureName(String featureName);
    // Dashboard
    long countByStatus(String status);
}
