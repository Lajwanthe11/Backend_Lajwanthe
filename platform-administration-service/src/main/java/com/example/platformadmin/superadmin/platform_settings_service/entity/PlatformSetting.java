package com.example.platformadmin.superadmin.platform_settings_service.entity;

import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "platform_settings",
        uniqueConstraints = {@UniqueConstraint(name = "uk_platform_settings_setting_name",
                                               columnNames = "setting_name") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "setting_name", nullable = false, length = 100)
    private String settingName;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SettingStatus status;

    @Column(name = "default_language", nullable = false)
    private String defaultLanguage;

    @Column(name = "default_time_zone", nullable = false)
    private String defaultTimeZone;

    @Column(name = "default_currency", nullable = false)
    private String defaultCurrency;

    @Column(name = "date_format", nullable = false)
    private String dateFormat;

    @Column(name = "time_format", nullable = false)
    private String timeFormat;

    @Column(name = "number_format", nullable = false)
    private String numberFormat;

    @Column(name = "session_timeout", nullable = false)
    private Integer sessionTimeout;

    @Column(name = "auto_logout", nullable = false)
    private Boolean autoLogout;

    @Column(name = "password_expiry", nullable = false)
    private Integer passwordExpiry;

    @Column(name = "maximum_login_attempts", nullable = false)
    private Integer maximumLoginAttempts;

    @Column(name = "maintenance_notification", nullable = false)
    private Boolean maintenanceNotification;

    @Column(name = "system_announcement", nullable = false)
    private Boolean systemAnnouncement;

    @Column(name = "multi_factor_authentication", nullable = false)
    private Boolean multiFactorAuthentication;

    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications;

    @Column(name = "sms_notifications", nullable = false)
    private Boolean smsNotifications;

    @Column(name = "push_notifications", nullable = false)
    private Boolean pushNotifications;

    @Column(name = "maximum_file_upload_size")
    private Long maximumFileUploadSize;

    @Column(name = "default_theme")
    private String defaultTheme;

    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode;

    @Column(name = "version_number", nullable = false)
    private Long versionNumber;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

}