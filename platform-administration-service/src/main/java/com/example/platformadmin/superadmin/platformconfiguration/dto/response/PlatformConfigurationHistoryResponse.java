package com.example.platformadmin.superadmin.platformconfiguration.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing an immutable historical snapshot of a Platform Configuration.
 *
 * <p><b>FRS Traceability:</b> BR-0016 (Revision History &amp; Rollback), AC-0011 (Audit Tracking).</p>
 *
 * <p>Captures point-in-time configuration values at the moment an operation (create, update,
 * status change, default restore) occurred. Contains {@link LocalDateTime} recordedAt timestamp.</p>
 *
 * @param historyId             unique primary key identifier of the snapshot (UUID)
 * @param configurationId       foreign identifier linking to the parent configuration
 * @param version               revision version captured at the time of this snapshot
 * @param configurationName     configuration name at time of snapshot
 * @param configurationCategory category at time of snapshot
 * @param description           description at time of snapshot
 * @param status                status at time of snapshot
 * @param effectiveDate         effective date at time of snapshot
 * @param platformName          platform name at time of snapshot
 * @param platformUrl           platform URL at time of snapshot
 * @param environment           environment tier at time of snapshot
 * @param defaultLanguage       language code at time of snapshot
 * @param defaultTimeZone       timezone at time of snapshot
 * @param defaultCurrency       currency at time of snapshot
 * @param maintenanceMode       maintenance mode at time of snapshot
 * @param featureToggle         feature toggle at time of snapshot
 * @param autoBackup            auto backup at time of snapshot
 * @param sessionTimeout        session timeout in minutes
 * @param passwordExpiry        password expiry in days
 * @param maximumLoginAttempts  max login attempts
 * @param recordedAt            timestamp when snapshot was created
 * @param recordedBy            username of user who triggered the snapshot
 * @param changeReason          audit explanation of the state change
 */
public record PlatformConfigurationHistoryResponse(
        UUID historyId,
        UUID configurationId,
        Integer version,
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
        LocalDateTime recordedAt,
        String recordedBy,
        String changeReason
) {
    /**
     * Backward-compatible constructor for callers providing LocalDate recordedAt.
     */
    public PlatformConfigurationHistoryResponse(
            UUID historyId, UUID configurationId, Integer version, String configurationName,
            String configurationCategory, String description, String status, LocalDate effectiveDate,
            String platformName, String platformUrl, String environment, String defaultLanguage,
            String defaultTimeZone, String defaultCurrency, String maintenanceMode, String featureToggle,
            String autoBackup, Integer sessionTimeout, Integer passwordExpiry, Integer maximumLoginAttempts,
            LocalDate recordedAt, String recordedBy, String changeReason) {
        this(historyId, configurationId, version, configurationName, configurationCategory, description,
                status, effectiveDate, platformName, platformUrl, environment, defaultLanguage,
                defaultTimeZone, defaultCurrency, maintenanceMode, featureToggle, autoBackup,
                sessionTimeout, passwordExpiry, maximumLoginAttempts,
                recordedAt != null ? recordedAt.atStartOfDay() : null,
                recordedBy, changeReason);
    }
}