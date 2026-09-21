package com.example.platformadmin.superadmin.platform_settings_service.service.validation;

import com.example.platformadmin.superadmin.platform_settings_service.dto.request.CreatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSetting;
import com.example.platformadmin.superadmin.platform_settings_service.exception.InvalidSettingException;
import org.springframework.stereotype.Service;

/**
 * Server-side business validation for Global Settings.
 * Bean validation protects the API boundary; this service protects the business layer.
 */
@Service
public class PlatformSettingsValidationService {

    // Validates the CreatePlatformSettingsRequest for required fields and value ranges.
    public void validateCreate(CreatePlatformSettingsRequest request) {

        requireText(request.getSettingName(), "Setting name is required");
        requireText(request.getCategory(), "Category is required");
        requireText(request.getDefaultLanguage(), "Default language is required");
        requireText(request.getDefaultTimeZone(), "Time zone is required");
        requireText(request.getDefaultCurrency(), "Default currency is required");
        requireText(request.getDateFormat(), "Date format is required");
        requireText(request.getTimeFormat(), "Time format is required");
        requireText(request.getNumberFormat(), "Number format is required");
        requireText(request.getDefaultTheme(), "Default theme is required");

        validateRange(request.getPasswordExpiry(), 30, 365,
                "Password expiry must be between 30 and 365 days");

        validateRange(request.getSessionTimeout(), 5, 240,
                "Session timeout must be between 5 and 240 minutes");

        validateRange(request.getMaximumLoginAttempts(), 3, 10,
                "Maximum login attempts must be between 3 and 10");

        if (request.getMaximumFileUploadSize() == null
                || request.getMaximumFileUploadSize() <= 0) {

            throw new InvalidSettingException("Maximum file upload size must be a positive value");
        }
    }

    // Validates the UpdatePlatformSettingsRequest for required fields and value ranges.
    public void validate(UpdatePlatformSettingsRequest request) {

        requireText(request.getDefaultLanguage(), "Default language is required");
        requireText(request.getDefaultTimeZone(), "Time zone is required");
        requireText(request.getDefaultCurrency(), "Default currency is required");
        requireText(request.getDateFormat(), "Date format is required");
        requireText(request.getTimeFormat(), "Time format is required");
        requireText(request.getNumberFormat(), "Number format is required");
        requireText(request.getDefaultTheme(), "Default theme is required");

        validateRange(request.getPasswordExpiry(), 30, 365,
                "Password expiry must be between 30 and 365 days");

        validateRange(request.getSessionTimeout(), 5, 240,
                "Session timeout must be between 5 and 240 minutes");

        validateRange(request.getMaximumLoginAttempts(), 3, 10,
                "Maximum login attempts must be between 3 and 10");

        if (request.getMaximumFileUploadSize() == null || request.getMaximumFileUploadSize() <= 0) {
            throw new InvalidSettingException("Maximum file upload size must be a positive value");
        }
    }

    // Validates the PlatformSetting entity for required fields and value ranges before activation.
    public void validateForActivation(PlatformSetting setting) {

        requireText(setting.getDefaultLanguage(), "Default language is required");
        requireText(setting.getDefaultTimeZone(), "Time zone is required");
        requireText(setting.getDefaultCurrency(), "Default currency is required");
        requireText(setting.getDateFormat(), "Date format is required");
        requireText(setting.getTimeFormat(), "Time format is required");
        requireText(setting.getNumberFormat(), "Number format is required");
        requireText(setting.getDefaultTheme(), "Default theme is required");

        validateRange(setting.getPasswordExpiry(), 30, 365,
                "Password expiry must be between 30 and 365 days");

        validateRange(setting.getSessionTimeout(), 5, 240,
                "Session timeout must be between 5 and 240 minutes");

        validateRange(setting.getMaximumLoginAttempts(), 3, 10,
                "Maximum login attempts must be between 3 and 10");

        if (setting.getMaximumFileUploadSize() == null || setting.getMaximumFileUploadSize() <= 0) {
            throw new InvalidSettingException("Maximum file upload size must be a positive value");
        }
    }


    // Helper method to validate that a numeric value is within a specified range.
    private void validateRange(Integer value, int min, int max, String message) {
        if (value == null || value < min || value > max) {
            throw new InvalidSettingException(message);
        }
    }

    // Helper method to validate that a string value is not null or blank.
    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidSettingException(message);
        }
    }

}