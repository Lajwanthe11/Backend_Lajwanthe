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
 * JPA Entity representing an Enterprise Platform Configuration under Platform Administration (FR-001.2).
 *
 * <p><b>Enterprise Capabilities:</b></p>
 * <ul>
 *   <li><b>Identity &amp; Lifecycle:</b> Unique UUID primary key, status state machine (ACTIVE / INACTIVE),
 *       and soft deletion tracking ({@code is_deleted}, {@code deleted_at}, {@code deleted_by}).</li>
 *   <li><b>Optimistic Locking &amp; Versioning:</b> Monotonically increasing {@code version} integer column.</li>
 *   <li><b>Audit Trail:</b> High-precision {@link LocalDateTime} timestamps ({@code created_at}, {@code updated_at})
 *       mapped to PostgreSQL {@code TIMESTAMPTZ} columns.</li>
 *   <li><b>Database Safeguards:</b> Protected by partial unique index {@code uq_platform_config_active_name}
 *       enforcing configuration name uniqueness exclusively among active records.</li>
 * </ul>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "platform_configurations")
public class PlatformConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

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

    // Audit Metadata (High-precision LocalDateTime timestamps)
    @NotNull(message = "Created at is required")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotBlank(message = "Created by is required")
    @Size(max = 50, message = "Created by must not exceed 50 characters")
    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @NotNull(message = "Updated at is required")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotBlank(message = "Updated by is required")
    @Size(max = 50, message = "Updated by must not exceed 50 characters")
    @Column(name = "updated_by", length = 50, nullable = false)
    private String updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Size(max = 50, message = "Deleted by must not exceed 50 characters")
    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @NotNull(message = "Version is required")
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * Backward-compatible constructor accepting LocalDate timestamps (for tests and legacy callers).
     */
    public PlatformConfiguration(
            UUID id, String configurationName, String configurationCategory, String description,
            String status, LocalDate effectiveDate, String platformName, String platformUrl,
            String environment, String defaultLanguage, String defaultTimeZone, String defaultCurrency,
            String maintenanceMode, String featureToggle, String autoBackup,
            Integer sessionTimeout, Integer passwordExpiry, Integer maximumLoginAttempts,
            LocalDate createdAt, String createdBy, LocalDate updatedAt, String updatedBy,
            boolean deleted, LocalDate deletedAt, String deletedBy) {
        this(id, configurationName, configurationCategory, description, status, effectiveDate,
                platformName, platformUrl, environment, defaultLanguage, defaultTimeZone, defaultCurrency,
                maintenanceMode, featureToggle, autoBackup, sessionTimeout, passwordExpiry, maximumLoginAttempts,
                createdAt != null ? createdAt.atStartOfDay() : null, createdBy,
                updatedAt != null ? updatedAt.atStartOfDay() : null, updatedBy,
                deleted,
                deletedAt != null ? deletedAt.atStartOfDay() : null, deletedBy,
                1);
    }

    // =========================================================================
    // EXPLICIT GETTERS AND SETTERS (Ensures IDE & Compiler Compatibility)
    // =========================================================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt != null ? createdAt.atStartOfDay() : null;
    }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt != null ? updatedAt.atStartOfDay() : null;
    }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public boolean isDeleted() { return deleted; }
    public boolean getDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public void setDeletedAt(LocalDate deletedAt) {
        this.deletedAt = deletedAt != null ? deletedAt.atStartOfDay() : null;
    }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformConfiguration that = (PlatformConfiguration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}