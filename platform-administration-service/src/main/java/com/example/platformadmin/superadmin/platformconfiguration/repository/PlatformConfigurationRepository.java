package com.example.platformadmin.superadmin.platformconfiguration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.platformadmin.superadmin.platformconfiguration.entity.PlatformConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Platform Configuration persistence operations.
 *
 * <p>
 * This repository provides database access for the
 * {@link PlatformConfiguration} entity.
 * </p>
 *
 * <p>
 * Platform configurations use soft deletion through the
 * {@code is_deleted} database column. Therefore, the retrieval
 * methods used by the service explicitly exclude soft-deleted
 * configurations.
 * </p>
 *
 * <p>
 * Business logic belongs in the service layer. This repository
 * is limited to persistence and query operations.
 * </p>
 */
@Repository
public interface PlatformConfigurationRepository
        extends JpaRepository<PlatformConfiguration, UUID>, JpaSpecificationExecutor<PlatformConfiguration>  {

    /**
     * Retrieves all non-deleted platform configurations.
     *
     * <p>
     * Used by:
     * GET /api/v1/platform-configurations
     * </p>
     *
     * @return list of non-deleted platform configurations
     */
    List<PlatformConfiguration> findAllByDeletedFalse();

    /**
     * Retrieves a non-deleted platform configuration by its identifier.
     *
     * <p>
     * A soft-deleted configuration is intentionally excluded.
     * </p>
     *
     * @param id unique identifier of the platform configuration
     * @return the matching non-deleted configuration, if present
     */
    Optional<PlatformConfiguration> findByIdAndDeletedFalse(UUID id);

    /**
     * Checks whether a non-deleted platform configuration already
     * exists with the specified configuration name (case-insensitive).
     *
     * @param configurationName configuration name to check
     * @return true if a non-deleted configuration already has the name;
     *         otherwise false
     */
    boolean existsByConfigurationNameIgnoreCaseAndDeletedFalse(
            String configurationName
    );

    /**
     * Checks whether another non-deleted platform configuration already
     * exists with the specified configuration name (case-insensitive).
     *
     * @param configurationName configuration name to check
     * @param id identifier of the configuration being updated
     * @return true if another non-deleted configuration already has
     *         the name; otherwise false
     */
    boolean existsByConfigurationNameIgnoreCaseAndDeletedFalseAndIdNot(
            String configurationName,
            UUID id
    );

    /**
     * Checks whether a non-deleted platform configuration already
     * exists with the specified configuration name.
     *
     * <p>
     * Configuration Name is defined as unique for platform configuration.
     * </p>
     *
     * @param configurationName configuration name to check
     * @return true if a non-deleted configuration already has the name;
     *         otherwise false
     */
    boolean existsByConfigurationNameAndDeletedFalse(
            String configurationName
    );

    /**
     * Checks whether another non-deleted platform configuration already
     * exists with the specified configuration name.
     *
     * <p>
     * The supplied identifier is excluded from the check so that an
     * existing configuration can retain its own name during an update.
     * </p>
     *
     * @param configurationName configuration name to check
     * @param id identifier of the configuration being updated
     * @return true if another non-deleted configuration already has
     *         the name; otherwise false
     */
    boolean existsByConfigurationNameAndDeletedFalseAndIdNot(
            String configurationName,
            UUID id
    );
}