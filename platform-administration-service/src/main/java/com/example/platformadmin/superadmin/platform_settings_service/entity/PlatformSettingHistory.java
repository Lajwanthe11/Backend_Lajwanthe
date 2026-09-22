package com.example.platformadmin.superadmin.platform_settings_service.entity;

import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingAction;
import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "platform_settings_history",
        uniqueConstraints = {@UniqueConstraint(name = "uk_setting_history_version",
                                               columnNames = {"setting_id", "version_number"})})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    private UUID historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private PlatformSetting setting;

    @Column(name = "version_number", nullable = false)
    private Long versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private SettingAction action;

    @Column(name = "activity_status", nullable = false, length = 30)
    private String activityStatus;

    @Column(name = "setting_name", nullable = false)
    private String settingName;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
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

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

}