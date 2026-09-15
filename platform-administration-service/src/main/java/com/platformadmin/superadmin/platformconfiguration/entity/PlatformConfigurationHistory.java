package com.example.platformadmin.superadmin.platformconfiguration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity capturing an immutable point-in-time snapshot of a Platform Configuration.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 *   <li><b>BR-0016:</b> Maintains a non-destructive audit history of configuration states across revisions.</li>
 *   <li><b>AC-0011:</b> Automatically recorded on every create, update, status change, and default restoration.</li>
 * </ul>
 *
 * <p>Stored in the {@code platform_configuration_history} table with high-precision {@link LocalDateTime}
 * timestamp {@code recorded_at} and fast version-ordered queries backed by {@code idx_platform_config_history_lookup}.</p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "platform_configuration_history")
public class PlatformConfigurationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "Configuration ID is required")
    @Column(name = "configuration_id", nullable = false)
    private UUID configurationId;

    @NotNull(message = "Version is required")
    @Column(name = "version", nullable = false)
    private Integer version;

    @NotBlank(message = "Configuration name is required")
    @Size(max = 100, message = "Configuration name must not exceed 100 characters")
    @Column(name = "configuration_name", length = 100, nullable = false)
    private String configurationName;

    @NotBlank(message = "Configuration category is required")
    @Size(max = 100, message = "Configuration category must not exceed 100 characters")
    @Column(name = "configuration_category", length = 100, nullable = false)
    private String configurationCategory;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @NotNull(message = "Effective date is required")
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    // Platform Settings
    @NotBlank(message = "Platform name is required")
    @Size(max = 150, message = "Platform name must not exceed 150 characters")
    @Column(name = "platform_name", length = 150, nullable = false)
    private String platformName;

    @NotBlank(message = "Platform URL is required")
    @URL(message = "Platform URL must be a valid URL")
    @Column(name = "platform_url", nullable = false)
    private String platformUrl;

    @NotBlank(message = "Environment is required")
    @Size(max = 20, message = "Environment must not exceed 20 characters")
    @Column(name = "environment", length = 20, nullable = false)
    private String environment;

    @NotBlank(message = "Default language is required")
    @Column(name = "default_language", nullable = false)
    private String defaultLanguage;

    @NotBlank(message = "Default time zone is required")
    @Column(name = "default_time_zone", nullable = false)
    private String defaultTimeZone;

    @NotBlank(message = "Default currency is required")
    @Column(name = "default_currency", nullable = false)
    private String defaultCurrency;

    // Operational Settings
    @NotBlank(message = "Maintenance mode is required")
    @Size(max = 10, message = "Maintenance mode must not exceed 10 characters")
    @Column(name = "maintenance_mode", length = 10, nullable = false)
    private String maintenanceMode;

    @NotBlank(message = "Feature toggle is required")
    @Size(max = 10, message = "Feature toggle must not exceed 10 characters")
    @Column(name = "feature_toggle", length = 10, nullable = false)
    private String featureToggle;

    @NotBlank(message = "Auto backup is required")
    @Size(max = 10, message = "Auto backup must not exceed 10 characters")
    @Column(name = "auto_backup", length = 10, nullable = false)
    private String autoBackup;

    @NotNull(message = "Session timeout is required")
    @Positive(message = "Session timeout must be a positive integer")
    @Column(name = "session_timeout", nullable = false)
    private Integer sessionTimeout;

    @NotNull(message = "Password expiry is required")
    @Positive(message = "Password expiry must be a positive integer")
    @Column(name = "password_expiry", nullable = false)
    private Integer passwordExpiry;

    @NotNull(message = "Maximum login attempts is required")
    @Positive(message = "Maximum login attempts must be a positive integer")
    @Column(name = "maximum_login_attempts", nullable = false)
    private Integer maximumLoginAttempts;

    // Snapshot Audit Metadata (LocalDateTime)
    @NotNull(message = "Recorded at is required")
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @NotBlank(message = "Recorded by is required")
    @Size(max = 50, message = "Recorded by must not exceed 50 characters")
    @Column(name = "recorded_by", length = 50, nullable = false)
    private String recordedBy;

    @Size(max = 100, message = "Change reason must not exceed 100 characters")
    @Column(name = "change_reason", length = 100)
    private String changeReason;

    /**
     * Static factory method to create a history snapshot from an active configuration.
     */
    public static PlatformConfigurationHistory from(PlatformConfiguration config, String changeReason, String recordedBy) {
        PlatformConfigurationHistory h = new PlatformConfigurationHistory();
        h.setConfigurationId(config.getId());
        h.setVersion(config.getVersion());
        h.setConfigurationName(config.getConfigurationName());
        h.setConfigurationCategory(config.getConfigurationCategory());
        h.setDescription(config.getDescription());
        h.setStatus(config.getStatus());
        h.setEffectiveDate(config.getEffectiveDate());
        h.setPlatformName(config.getPlatformName());
        h.setPlatformUrl(config.getPlatformUrl());
        h.setEnvironment(config.getEnvironment());
        h.setDefaultLanguage(config.getDefaultLanguage());
        h.setDefaultTimeZone(config.getDefaultTimeZone());
        h.setDefaultCurrency(config.getDefaultCurrency());
        h.setMaintenanceMode(config.getMaintenanceMode());
        h.setFeatureToggle(config.getFeatureToggle());
        h.setAutoBackup(config.getAutoBackup());
        h.setSessionTimeout(config.getSessionTimeout());
        h.setPasswordExpiry(config.getPasswordExpiry());
        h.setMaximumLoginAttempts(config.getMaximumLoginAttempts());
        h.setRecordedAt(LocalDateTime.now());
        h.setRecordedBy(recordedBy);
        h.setChangeReason(changeReason);
        return h;
    }

    // =========================================================================
    // EXPLICIT GETTERS AND SETTERS (Ensures IDE & Compiler Compatibility)
    // =========================================================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConfigurationId() { return configurationId; }
    public void setConfigurationId(UUID configurationId) { this.configurationId = configurationId; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getConfigurationName() { return configurationName; }
    public void setConfigurationName(String configurationName) { this.configurationName = configurationName; }

    public String getConfigurationCategory() { return configurationCategory; }
    public void setConfigurationCategory(String configurationCategory) { this.configurationCategory = configurationCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getPlatformUrl() { return platformUrl; }
    public void setPlatformUrl(String platformUrl) { this.platformUrl = platformUrl; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getDefaultLanguage() { return defaultLanguage; }
    public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }

    public String getDefaultTimeZone() { return defaultTimeZone; }
    public void setDefaultTimeZone(String defaultTimeZone) { this.defaultTimeZone = defaultTimeZone; }

    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }

    public String getMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(String maintenanceMode) { this.maintenanceMode = maintenanceMode; }

    public String getFeatureToggle() { return featureToggle; }
    public void setFeatureToggle(String featureToggle) { this.featureToggle = featureToggle; }

    public String getAutoBackup() { return autoBackup; }
    public void setAutoBackup(String autoBackup) { this.autoBackup = autoBackup; }

    public Integer getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(Integer sessionTimeout) { this.sessionTimeout = sessionTimeout; }

    public Integer getPasswordExpiry() { return passwordExpiry; }
    public void setPasswordExpiry(Integer passwordExpiry) { this.passwordExpiry = passwordExpiry; }

    public Integer getMaximumLoginAttempts() { return maximumLoginAttempts; }
    public void setMaximumLoginAttempts(Integer maximumLoginAttempts) { this.maximumLoginAttempts = maximumLoginAttempts; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public void setRecordedAt(LocalDate recordedAt) {
        this.recordedAt = recordedAt != null ? recordedAt.atStartOfDay() : null;
    }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformConfigurationHistory that = (PlatformConfigurationHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}