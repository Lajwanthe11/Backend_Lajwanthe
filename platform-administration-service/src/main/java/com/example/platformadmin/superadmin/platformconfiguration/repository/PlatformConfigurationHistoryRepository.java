package com.example.platformadmin.superadmin.platformconfiguration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.platformadmin.superadmin.platformconfiguration.entity.PlatformConfigurationHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for managing historical audit snapshots of Platform Configurations.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 *   <li><b>BR-0016:</b> Complete revision history tracking with point-in-time state recovery.</li>
 *   <li><b>AC-0011:</b> Every create, update, status change, and default restore operation captures an immutable snapshot.</li>
 * </ul>
 *
 * <p>Supported by database index {@code idx_platform_config_history_lookup} on {@code (configuration_id, version DESC)}.</p>
 */
@Repository
public interface PlatformConfigurationHistoryRepository extends JpaRepository<PlatformConfigurationHistory, UUID> {

    /**
     * Retrieves all history snapshots for a specific configuration, ordered by version descending.
     *
     * @param configurationId target configuration identifier
     * @return list of snapshots from latest to oldest
     */
    List<PlatformConfigurationHistory> findAllByConfigurationIdOrderByVersionDesc(UUID configurationId);

    /**
     * Retrieves a specific version snapshot of a configuration.
     *
     * @param configurationId target configuration identifier
     * @param version         version number to retrieve
     * @return matching snapshot, if present
     */
    Optional<PlatformConfigurationHistory> findByConfigurationIdAndVersion(UUID configurationId, Integer version);
}
