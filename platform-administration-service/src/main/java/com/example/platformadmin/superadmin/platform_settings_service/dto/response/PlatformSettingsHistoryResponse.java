package com.example.platformadmin.superadmin.platform_settings_service.dto.response;

import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingAction;
import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

// Snapshot returned by the Platform Settings configuration history API.
@Getter
@Builder
public class PlatformSettingsHistoryResponse {

    private UUID historyId;
    private UUID settingId;
    private Long versionNumber;
    private SettingAction action;
    private String activityStatus;

    private String settingName;
    private String category;
    private String description;
    private SettingStatus status;

    private String defaultLanguage;
    private String defaultTimeZone;
    private String defaultCurrency;
    private String dateFormat;
    private String timeFormat;
    private String numberFormat;

    private Integer sessionTimeout;
    private Boolean autoLogout;
    private Integer passwordExpiry;
    private Integer maximumLoginAttempts;
    private Boolean maintenanceNotification;
    private Boolean systemAnnouncement;

    private Boolean multiFactorAuthentication;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean pushNotifications;

    private Long maximumFileUploadSize;
    private String defaultTheme;
    private Boolean maintenanceMode;

    private String userId;
    private String userName;
    private String ipAddress;
    private OffsetDateTime changedAt;

}