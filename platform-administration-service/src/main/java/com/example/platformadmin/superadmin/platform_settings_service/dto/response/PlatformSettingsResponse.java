package com.example.platformadmin.superadmin.platform_settings_service.dto.response;

import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

// Represents the API response returned to clients.
@Getter
@Builder
public class PlatformSettingsResponse {

    private UUID id;
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

    private Long versionNumber;
    private OffsetDateTime updatedAt;
    private String updatedBy;

}