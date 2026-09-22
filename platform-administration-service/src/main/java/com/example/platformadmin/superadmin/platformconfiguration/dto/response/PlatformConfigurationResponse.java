package com.example.platformadmin.superadmin.platformconfiguration.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing the serialized state of an enterprise platform configuration.
 *
 * <p><b>FRS Traceability:</b> FR-001.2 Platform Configuration Management.</p>
 *
 * <p>Exposes all platform settings, operational configuration parameters, audit metadata,
 * and optimistic locking revision version. Audit timestamps {@code createdAt} and {@code updatedAt}
 * are serialized as {@link LocalDateTime}.</p>
 *
 * @param id                    unique primary key identifier (UUID)
 * @param configurationName     globally unique active configuration name
 * @param configurationCategory business domain category (e.g. PLATFORM, SYSTEM)
 * @param description           descriptive text summarizing purpose
 * @param status                lifecycle state (ACTIVE or INACTIVE)
 * @param effectiveDate         date from which configuration takes effect
 * @param platformName          user-facing display platform name
 * @param platformUrl           platform web URL
 * @param environment           operational environment tier (DEVELOPMENT, TESTING, STAGING, PRODUCTION)
 * @param defaultLanguage       ISO 639-1 language code (e.g. en)
 * @param defaultTimeZone       IANA timezone identifier (e.g. UTC)
 * @param defaultCurrency       ISO 4217 currency code (e.g. USD)
 * @param maintenanceMode       operational maintenance toggle (ENABLED or DISABLED)
 * @param featureToggle         platform feature toggle (ENABLED or DISABLED)
 * @param autoBackup            automated backup status (ENABLED or DISABLED)
 * @param sessionTimeout        session inactivity timeout in minutes
 * @param passwordExpiry        password expiration period in days
 * @param maximumLoginAttempts  failed authentication lockout threshold
 * @param createdAt             immutable creation timestamp
 * @param createdBy             immutable username of user who created configuration
 * @param updatedAt             timestamp of last modification
 * @param updatedBy             username of user who performed last modification
 * @param version               optimistic concurrency version number
 */
public record PlatformConfigurationResponse(
        UUID id,
        String configurationName,
        String configurationCategory,
        String description,
        String status,
        LocalDate effectiveDate,
        String platformName,
        String platformUrl,
        String environment,
        String defaultLanguage,
        String defaultTimeZone,
        String defaultCurrency,
        String maintenanceMode,
        String featureToggle,
        String autoBackup,
        Integer sessionTimeout,
        Integer passwordExpiry,
        Integer maximumLoginAttempts,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        Integer version
) {
    /**
     * Backward-compatible constructor for callers providing 22 fields with LocalDateTime timestamps.
     */
    public PlatformConfigurationResponse(
            UUID id, String configurationName, String configurationCategory, String description,
            String status, LocalDate effectiveDate, String platformName, String platformUrl,
            String environment, String defaultLanguage, String defaultTimeZone, String defaultCurrency,
            String maintenanceMode, String featureToggle, String autoBackup,
            Integer sessionTimeout, Integer passwordExpiry, Integer maximumLoginAttempts,
            LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy) {
        this(id, configurationName, configurationCategory, description, status, effectiveDate,
                platformName, platformUrl, environment, defaultLanguage, defaultTimeZone, defaultCurrency,
                maintenanceMode, featureToggle, autoBackup, sessionTimeout, passwordExpiry, maximumLoginAttempts,
                createdAt, createdBy, updatedAt, updatedBy, 1);
    }

    /**
     * Backward-compatible constructor for callers providing 22 fields with LocalDate timestamps.
     */
    public PlatformConfigurationResponse(
            UUID id, String configurationName, String configurationCategory, String description,
            String status, LocalDate effectiveDate, String platformName, String platformUrl,
            String environment, String defaultLanguage, String defaultTimeZone, String defaultCurrency,
            String maintenanceMode, String featureToggle, String autoBackup,
            Integer sessionTimeout, Integer passwordExpiry, Integer maximumLoginAttempts,
            LocalDate createdAt, String createdBy, LocalDate updatedAt, String updatedBy) {
        this(id, configurationName, configurationCategory, description, status, effectiveDate,
                platformName, platformUrl, environment, defaultLanguage, defaultTimeZone, defaultCurrency,
                maintenanceMode, featureToggle, autoBackup, sessionTimeout, passwordExpiry, maximumLoginAttempts,
                createdAt != null ? createdAt.atStartOfDay() : null, createdBy,
                updatedAt != null ? updatedAt.atStartOfDay() : null, updatedBy, 1);
    }

    /**
     * Backward-compatible constructor for callers providing 23 fields with LocalDate timestamps.
     */
    public PlatformConfigurationResponse(
            UUID id, String configurationName, String configurationCategory, String description,
            String status, LocalDate effectiveDate, String platformName, String platformUrl,
            String environment, String defaultLanguage, String defaultTimeZone, String defaultCurrency,
            String maintenanceMode, String featureToggle, String autoBackup,
            Integer sessionTimeout, Integer passwordExpiry, Integer maximumLoginAttempts,
            LocalDate createdAt, String createdBy, LocalDate updatedAt, String updatedBy, Integer version) {
        this(id, configurationName, configurationCategory, description, status, effectiveDate,
                platformName, platformUrl, environment, defaultLanguage, defaultTimeZone, defaultCurrency,
                maintenanceMode, featureToggle, autoBackup, sessionTimeout, passwordExpiry, maximumLoginAttempts,
                createdAt != null ? createdAt.atStartOfDay() : null, createdBy,
                updatedAt != null ? updatedAt.atStartOfDay() : null, updatedBy, version);
    }
}