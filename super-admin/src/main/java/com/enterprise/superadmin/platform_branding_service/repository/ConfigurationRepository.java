package com.enterprise.superadmin.platform_branding_service.repository;

import com.enterprise.superadmin.platform_branding_service.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {

    /**
     * Find an active configuration by its complete logical identity.
     *
     * Logical identity:
     * - configKey
     * - category
     * - scope
     * - not deleted
     */
    Optional<Configuration> findByConfigKeyAndCategoryAndScopeAndIsDeletedFalse(String configKey, String category, String scope);

    /**
     * Return all active configurations belonging to a category.
     */
    List<Configuration> findByCategoryAndIsDeletedFalse(String category);

    /**
     * Return all active configurations for a category and scope.
     */
    List<Configuration> findByCategoryAndScopeAndIsDeletedFalse(String category, String scope);

    /**
     * Check whether an active configuration exists
     * for the complete logical identity.
     */
    boolean existsByConfigKeyAndCategoryAndScopeAndIsDeletedFalse(String configKey, String category, String scope);
}