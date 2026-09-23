package com.example.platformadmin.superadmin.platform_settings_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

// Request DTO used to create a new platform setting.
@Data
public class CreatePlatformSettingsRequest {

    @NotBlank(message = "Setting name is required")
    @Size(max = 100, message = "Setting name cannot exceed 100 characters")
    private String settingName;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Default language is required")
    private String defaultLanguage;

    @NotBlank(message = "Default time zone is required")
    private String defaultTimeZone;

    @NotBlank(message = "Default currency is required")
    private String defaultCurrency;

    @NotBlank(message = "Date format is required")
    private String dateFormat;

    @NotBlank(message = "Time format is required")
    private String timeFormat;

    @NotBlank(message = "Number format is required")
    private String numberFormat;

    @NotNull(message = "Session timeout is required")
    @Min(value = 5, message = "Session timeout must be at least 5 minutes")
    @Max(value = 240, message = "Session timeout cannot exceed 240 minutes")
    private Integer sessionTimeout;

    @NotNull(message = "Auto logout is required")
    private Boolean autoLogout;

    @NotNull(message = "Password expiry is required")
    @Min(value = 30, message = "Password expiry must be at least 30 days")
    @Max(value = 365, message = "Password expiry cannot exceed 365 days")
    private Integer passwordExpiry;

    @NotNull(message = "Maximum login attempts is required")
    @Min(value = 3, message = "Maximum login attempts must be at least 3")
    @Max(value = 10, message = "Maximum login attempts cannot exceed 10")
    private Integer maximumLoginAttempts;

    @NotNull(message = "Maintenance notification is required")
    private Boolean maintenanceNotification;

    @NotNull(message = "System announcement is required")
    private Boolean systemAnnouncement;

    @NotNull(message = "MFA setting is required")
    private Boolean multiFactorAuthentication;

    @NotNull(message = "Email notification setting is required")
    private Boolean emailNotifications;

    @NotNull(message = "SMS notification setting is required")
    private Boolean smsNotifications;

    @NotNull(message = "Push notification setting is required")
    private Boolean pushNotifications;

    @NotNull(message = "Maximum file upload size is required")
    @Positive(message = "Maximum file upload size must be positive")
    private Long maximumFileUploadSize;

    @NotBlank(message = "Default theme is required")
    private String defaultTheme;

    @NotNull(message = "Maintenance mode is required")
    private Boolean maintenanceMode;

}