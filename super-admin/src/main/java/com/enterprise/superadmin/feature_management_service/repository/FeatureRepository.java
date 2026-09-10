package com.enterprise.superadmin.feature_management_service.repository;



import com.enterprise.superadmin.feature_management_service.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, UUID> {

    Optional<Feature> findByFeatureName(String featureName);

    List<Feature> findByModule(String module);

    List<Feature> findByStatus(String status);

    List<Feature> findByLicensePlan(String licensePlan);

    boolean existsByFeatureName(String featureName);
}
