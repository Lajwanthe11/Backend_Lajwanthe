package com.example.platformadmin.superadmin.platform_settings_service.service;

import com.example.platformadmin.superadmin.platform_settings_service.dto.request.CreatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdateSettingStatusRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.PlatformSettingsHistoryResponse;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.PlatformSettingsResponse;
import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;

import java.util.List;

public interface PlatformSettingsService {

    PlatformSettingsResponse createSetting(CreatePlatformSettingsRequest request);

    List<PlatformSettingsResponse> getAllSettings(String search,
                                                  String category,
                                                  SettingStatus status);

    PlatformSettingsResponse getSetting(String key);

    PlatformSettingsResponse updateSetting(String key,
                                           UpdatePlatformSettingsRequest request);

    PlatformSettingsResponse updateStatus(String key,
                                          UpdateSettingStatusRequest request);

    PlatformSettingsResponse resetSettings();

    List<PlatformSettingsHistoryResponse> getSettingHistory(String key);

    byte[] exportSettings(String search,
                          String category,
                          SettingStatus status);

}