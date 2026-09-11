package com.example.platformadmin.superadmin.platformconfiguration.service;

import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationCreateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationStatusUpdateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationUpdateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.response.PlatformConfigurationHistoryResponse;
import com.example.platformadmin.superadmin.platformconfiguration.dto.response.PlatformConfigurationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface defining business operations for Platform Configuration management.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 *   <li><b>Module:</b> Module 1 &mdash; Platform Administration</li>
 *   <li><b>Feature:</b> FR-001.2 Platform Configuration</li>
 *   <li><b>Acceptance Criteria:</b> AC-0009 (Create/Update), AC-0010 (Validation),
 *       AC-0011 (Audit/History), AC-0015 (Defaults), AC-0016 (Status &amp; Soft Delete)</li>
 *   <li><b>Business Rules:</b> BR-0011 (Soft Delete), BR-0012 (Unique Active Name),
 *       BR-0016 (Revision History &amp; Rollback), BR-0017 (Validation Gate), BR-0020 (Pre-Activation Validation)</li>
 * </ul>
 */
public interface PlatformConfigurationService {

    /**
     * Searches and filters active (non-soft-deleted) platform configurations.
     *
     * @param name        optional configuration name (partial, case-insensitive match)
     * @param category    optional configuration category (exact, case-insensitive match)
     * @param environment optional environment tier (DEVELOPMENT, TESTING, STAGING, PRODUCTION)
     * @param status      optional lifecycle status (ACTIVE or INACTIVE)
     * @return list of matching {@link PlatformConfigurationResponse} DTOs
     */
    List<PlatformConfigurationResponse> getAll(String name, String category, String environment, String status);

    /**
     * Retrieves an active platform configuration by unique identifier.
     *
     * @param id unique identifier of the target configuration
     * @return matching {@link PlatformConfigurationResponse} DTO
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if configuration does not exist or is soft-deleted
     */
    PlatformConfigurationResponse getById(UUID id);

    /**
     * Creates and persists a new platform configuration after validating business constraints.
     *
     * @param request valid payload containing initial configuration values
     * @return created {@link PlatformConfigurationResponse} DTO with generated UUID and version 1
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.DuplicateConfigurationNameException
     *         if another active configuration already uses the specified name
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.InvalidConfigurationValueException
     *         if validation constraints or master data checks fail
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.ConfigurationActivationException
     *         if pre-activation validation fails when starting in ACTIVE status
     */
    PlatformConfigurationResponse create(PlatformConfigurationCreateRequest request);

    /**
     * Updates an existing non-deleted platform configuration.
     *
     * @param id      unique identifier of the configuration to update
     * @param request valid payload containing modified configuration values
     * @return updated {@link PlatformConfigurationResponse} DTO with incremented version
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if the configuration does not exist or is soft-deleted
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.DuplicateConfigurationNameException
     *         if the updated name conflicts with another active configuration
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.InvalidConfigurationValueException
     *         if validation constraints or master data checks fail
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.ConfigurationActivationException
     *         if pre-activation validation fails when updating to ACTIVE status
     */
    PlatformConfigurationResponse update(UUID id, PlatformConfigurationUpdateRequest request);

    /**
     * Updates the lifecycle status (ACTIVE or INACTIVE) of an existing platform configuration.
     *
     * @param id      unique identifier of the configuration
     * @param request payload containing target status
     * @return updated {@link PlatformConfigurationResponse} DTO with incremented version
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if the configuration does not exist or is soft-deleted
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.InvalidConfigurationValueException
     *         if status string is unrecognized
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.ConfigurationActivationException
     *         if pre-activation conditions fail when transitioning to ACTIVE
     */
    PlatformConfigurationResponse updateStatus(UUID id, PlatformConfigurationStatusUpdateRequest request);

    /**
     * Soft-deletes an existing platform configuration while preserving history snapshots.
     *
     * @param id unique identifier of the platform configuration to soft delete
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if the configuration does not exist or is already soft-deleted
     */
    void delete(UUID id);

    /**
     * Restores an existing platform configuration to standard enterprise default settings.
     *
     * @param id unique identifier of the platform configuration to restore
     * @return restored {@link PlatformConfigurationResponse} DTO with incremented version
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if the configuration does not exist or is soft-deleted
     */
    PlatformConfigurationResponse restoreDefaultConfiguration(UUID id);

    /**
     * Retrieves the complete version revision history for a configuration.
     *
     * @param id configuration unique identifier
     * @return list of historical snapshots ordered descending by version
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if the configuration does not exist or is soft-deleted
     */
    List<PlatformConfigurationHistoryResponse> getHistory(UUID id);

    /**
     * Rolls back an active configuration to an earlier version snapshot using Security Context user.
     *
     * @param id      configuration unique identifier
     * @param version target version number to restore
     * @return updated configuration with incremented version reflecting restored state
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if the configuration or target version does not exist
     */
    PlatformConfigurationResponse rollbackToVersion(UUID id, int version);

    /**
     * Rolls back an active configuration to an earlier version snapshot with explicit updater identity.
     *
     * @param id        configuration unique identifier
     * @param version   target version number to restore
     * @param updatedBy explicit username performing the rollback
     * @return updated configuration with incremented version reflecting restored state
     * @throws com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException
     *         if the configuration or target version does not exist
     */
    PlatformConfigurationResponse rollbackToVersion(UUID id, int version, String updatedBy);
}