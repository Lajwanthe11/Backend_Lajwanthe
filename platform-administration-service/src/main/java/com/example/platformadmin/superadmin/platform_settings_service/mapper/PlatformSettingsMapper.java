package com.example.platformadmin.superadmin.platform_settings_service.mapper;

import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.PlatformSettingsResponse;
import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSetting;
import org.springframework.stereotype.Component;

@Component
public class PlatformSettingsMapper {

    // Converts a PlatformSetting entity into a PlatformSettingsResponse DTO.
    public PlatformSettingsResponse toResponse(PlatformSetting entity) {

        return PlatformSettingsResponse.builder()
                .id(entity.getId())
                .settingName(entity.getSettingName())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .defaultLanguage(entity.getDefaultLanguage())
                .defaultTimeZone(entity.getDefaultTimeZone())
                .defaultCurrency(entity.getDefaultCurrency())
                .dateFormat(entity.getDateFormat())
                .timeFormat(entity.getTimeFormat())
                .numberFormat(entity.getNumberFormat())
                .sessionTimeout(entity.getSessionTimeout())
                .autoLogout(entity.getAutoLogout())
                .passwordExpiry(entity.getPasswordExpiry())
                .maximumLoginAttempts(entity.getMaximumLoginAttempts())
                .maintenanceNotification(entity.getMaintenanceNotification())
                .systemAnnouncement(entity.getSystemAnnouncement())
                .multiFactorAuthentication(entity.getMultiFactorAuthentication())
                .emailNotifications(entity.getEmailNotifications())
                .smsNotifications(entity.getSmsNotifications())
                .pushNotifications(entity.getPushNotifications())
                .maximumFileUploadSize(entity.getMaximumFileUploadSize())
                .defaultTheme(entity.getDefaultTheme())
                .maintenanceMode(entity.getMaintenanceMode())
                .versionNumber(entity.getVersionNumber())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // Updates an existing entity with new values from the request.
    public void updateEntity(PlatformSetting entity, UpdatePlatformSettingsRequest request) {

        entity.setDescription(request.getDescription());
        entity.setDefaultLanguage(request.getDefaultLanguage());
        entity.setDefaultTimeZone(request.getDefaultTimeZone());
        entity.setDefaultCurrency(request.getDefaultCurrency());
        entity.setDateFormat(request.getDateFormat());
        entity.setTimeFormat(request.getTimeFormat());
        entity.setNumberFormat(request.getNumberFormat());
        entity.setSessionTimeout(request.getSessionTimeout());
        entity.setAutoLogout(request.getAutoLogout());
        entity.setPasswordExpiry(request.getPasswordExpiry());
        entity.setMaximumLoginAttempts(request.getMaximumLoginAttempts());
        entity.setMaintenanceNotification(request.getMaintenanceNotification());
        entity.setSystemAnnouncement(request.getSystemAnnouncement());
        entity.setMultiFactorAuthentication(request.getMultiFactorAuthentication());
        entity.setEmailNotifications(request.getEmailNotifications());
        entity.setSmsNotifications(request.getSmsNotifications());
        entity.setPushNotifications(request.getPushNotifications());
        entity.setMaximumFileUploadSize(request.getMaximumFileUploadSize());
        entity.setDefaultTheme(request.getDefaultTheme());
        entity.setMaintenanceMode(request.getMaintenanceMode());
    }

}