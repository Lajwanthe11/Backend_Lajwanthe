package com.example.platformadmin.superadmin.platform_settings_service.service.impl;

import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdateSettingStatusRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.PlatformSettingsHistoryResponse;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.PlatformSettingsResponse;
import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSetting;
import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSettingHistory;
import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingAction;
import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import com.example.platformadmin.superadmin.platform_settings_service.exception.InvalidSettingException;
import com.example.platformadmin.superadmin.platform_settings_service.exception.SettingNotFoundException;
import com.example.platformadmin.superadmin.platform_settings_service.integration.PlatformSettingsAuditIntegration;
import com.example.platformadmin.superadmin.platform_settings_service.integration.PlatformSettingsPropagationIntegration;
import com.example.platformadmin.superadmin.platform_settings_service.mapper.PlatformSettingsMapper;
import com.example.platformadmin.superadmin.platform_settings_service.repository.PlatformSettingHistoryRepository;
import com.example.platformadmin.superadmin.platform_settings_service.repository.PlatformSettingRepository;
import com.example.platformadmin.superadmin.platform_settings_service.security.CurrentUserProvider;
import com.example.platformadmin.superadmin.platform_settings_service.service.PlatformSettingsService;
import com.example.platformadmin.superadmin.platform_settings_service.service.validation.PlatformSettingsValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    private static final String GLOBAL_SETTINGS = "GLOBAL_SETTINGS";
    private static final String MODULE = "PLATFORM_SETTINGS";
    private static final String ENTITY_TYPE = "GLOBAL_SETTINGS";
    private static final String SUCCESS = "SUCCESS";

    private final PlatformSettingRepository settingRepository;
    private final PlatformSettingHistoryRepository historyRepository;
    private final PlatformSettingsMapper mapper;
    private final CurrentUserProvider currentUserProvider;
    private final PlatformSettingsValidationService validationService;
    private final PlatformSettingsPropagationIntegration propagationIntegration;
    private final PlatformSettingsAuditIntegration auditIntegration;

    // ---------------------------------------------------------
    // Get All Settings
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PlatformSettingsResponse> getAllSettings(String search, String category, SettingStatus status) {

        log.info("Fetching platform settings. search={}, category={}, status={}", search, category, status);

        String normalizedSearch = normalizeFilter(search);
        String normalizedCategory = normalizeFilter(category);

        return settingRepository.searchAndFilter(normalizedSearch, normalizedCategory, status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // Get Setting by Key
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PlatformSettingsResponse getSetting(String key) {

        log.info("Fetching platform setting. key={}", key);

        return mapper.toResponse(findSetting(key));
    }

    // ---------------------------------------------------------
    // Update Setting
    // ---------------------------------------------------------

    @Override
    public PlatformSettingsResponse updateSetting(String key, UpdatePlatformSettingsRequest request) {

        log.info("Updating platform setting. key={}", key);

        PlatformSetting setting = findSetting(key);
        validationService.validate(request);
        mapper.updateEntity(setting, request);

        // A save/update creates a validated version. It becomes effective only after activation.
        setting.setStatus(SettingStatus.VALIDATED);
        setting.setVersionNumber(setting.getVersionNumber() + 1);
        setting.setUpdatedAt(now());
        setting.setUpdatedBy(currentUserProvider.getUserName());

        PlatformSetting saved = settingRepository.save(setting);

        recordHistoryAndAudit(saved, SettingAction.MODIFIED);

        log.info("Platform setting updated and validated. key={}, version={}",
                key, saved.getVersionNumber());

        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------
    // Update Setting Status
    // ---------------------------------------------------------

    @Override
    public PlatformSettingsResponse updateStatus(String key, UpdateSettingStatusRequest request) {

        log.info("Updating platform setting status. key={}, status={}", key, request.getStatus());

        PlatformSetting setting = findSetting(key);

        if (request.getStatus() != SettingStatus.ACTIVE
                && request.getStatus() != SettingStatus.INACTIVE) {
            throw new InvalidSettingException("Status must be ACTIVE or INACTIVE");
        }

        if (request.getStatus() == SettingStatus.ACTIVE) {
            validationService.validateForActivation(setting);
        }

        setting.setStatus(request.getStatus());
        setting.setVersionNumber(setting.getVersionNumber() + 1);
        setting.setUpdatedAt(now());
        setting.setUpdatedBy(currentUserProvider.getUserName());

        PlatformSetting saved = settingRepository.save(setting);

        // Activation is the point at which the new settings become effective.
        if (saved.getStatus() == SettingStatus.ACTIVE) {
            propagationIntegration.propagate(saved);
        }

        SettingAction action = saved.getStatus() == SettingStatus.ACTIVE
                ? SettingAction.ACTIVATED
                : SettingAction.MODIFIED;

        recordHistoryAndAudit(saved, action);

        log.info("Platform setting status updated successfully. key={}, status={}, version={}",
                key, saved.getStatus(), saved.getVersionNumber());

        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------
    // Reset Settings to Defaults
    // ---------------------------------------------------------

    @Override
    public PlatformSettingsResponse resetSettings() {
        log.info("Restoring global platform settings to defaults");

        PlatformSetting setting = findSetting(GLOBAL_SETTINGS);
        applyDefaults(setting);
        validationService.validateForActivation(setting);

        setting.setVersionNumber(setting.getVersionNumber() + 1);
        setting.setStatus(SettingStatus.ACTIVE);
        setting.setUpdatedAt(now());
        setting.setUpdatedBy(currentUserProvider.getUserName());

        PlatformSetting saved = settingRepository.save(setting);

        propagationIntegration.propagate(saved);
        recordHistoryAndAudit(saved, SettingAction.RESTORED);

        log.info("Global platform settings restored successfully. version={}",
                saved.getVersionNumber());

        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------
    // Get Setting History
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PlatformSettingsHistoryResponse> getSettingHistory(String key) {
        PlatformSetting setting = findSetting(key);

        log.info("Fetching platform settings history. key={}", key);

        return historyRepository.findBySettingIdOrderByVersionNumberDesc(setting.getId())
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // Export Settings
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSettings(String search, String category, SettingStatus status) {
        log.info("Exporting platform settings. search={}, category={}, status={}", search, category, status);

        String normalizedSearch = normalizeFilter(search);
        String normalizedCategory = normalizeFilter(category);

        List<PlatformSetting> settings = settingRepository.searchAndFilter(
                normalizedSearch, normalizedCategory, status);

        StringBuilder csv = new StringBuilder();
        csv.append("Setting ID,Setting Name,Category,Description,Status,Default Language,Default Time Zone,")
                .append("Default Currency,Date Format,Time Format,Number Format,Session Timeout,Auto Logout,")
                .append("Password Expiry,Maximum Login Attempts,Maintenance Notification,System Announcement,")
                .append("Multi Factor Authentication,Email Notifications,SMS Notifications,Push Notifications,")
                .append("Maximum File Upload Size,Default Theme,Maintenance Mode,Version Number,Updated At,Updated By")
                .append('\n');

        for (PlatformSetting setting : settings) {
            csv.append(csvValue(setting.getId()))
                    .append(',').append(csvValue(setting.getSettingName()))
                    .append(',').append(csvValue(setting.getCategory()))
                    .append(',').append(csvValue(setting.getDescription()))
                    .append(',').append(csvValue(setting.getStatus()))
                    .append(',').append(csvValue(setting.getDefaultLanguage()))
                    .append(',').append(csvValue(setting.getDefaultTimeZone()))
                    .append(',').append(csvValue(setting.getDefaultCurrency()))
                    .append(',').append(csvValue(setting.getDateFormat()))
                    .append(',').append(csvValue(setting.getTimeFormat()))
                    .append(',').append(csvValue(setting.getNumberFormat()))
                    .append(',').append(csvValue(setting.getSessionTimeout()))
                    .append(',').append(csvValue(setting.getAutoLogout()))
                    .append(',').append(csvValue(setting.getPasswordExpiry()))
                    .append(',').append(csvValue(setting.getMaximumLoginAttempts()))
                    .append(',').append(csvValue(setting.getMaintenanceNotification()))
                    .append(',').append(csvValue(setting.getSystemAnnouncement()))
                    .append(',').append(csvValue(setting.getMultiFactorAuthentication()))
                    .append(',').append(csvValue(setting.getEmailNotifications()))
                    .append(',').append(csvValue(setting.getSmsNotifications()))
                    .append(',').append(csvValue(setting.getPushNotifications()))
                    .append(',').append(csvValue(setting.getMaximumFileUploadSize()))
                    .append(',').append(csvValue(setting.getDefaultTheme()))
                    .append(',').append(csvValue(setting.getMaintenanceMode()))
                    .append(',').append(csvValue(setting.getVersionNumber()))
                    .append(',').append(csvValue(setting.getUpdatedAt()))
                    .append(',').append(csvValue(setting.getUpdatedBy()))
                    .append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Helper methods

    private PlatformSetting findSetting(String key) {
        return settingRepository.findBySettingName(key)
                .orElseThrow(() -> new SettingNotFoundException("Platform setting not found: " + key));
    }

    private void applyDefaults(PlatformSetting setting) {
        setting.setDefaultLanguage("English");
        setting.setDefaultTimeZone("Asia/Kolkata");
        setting.setDefaultCurrency("INR");
        setting.setDateFormat("DD/MM/YYYY");
        setting.setTimeFormat("24 Hours");
        setting.setNumberFormat("#,##0.00");
        setting.setSessionTimeout(30);
        setting.setAutoLogout(true);
        setting.setPasswordExpiry(90);
        setting.setMaximumLoginAttempts(5);
        setting.setMaintenanceNotification(false);
        setting.setSystemAnnouncement(true);
        setting.setMultiFactorAuthentication(true);
        setting.setEmailNotifications(true);
        setting.setSmsNotifications(true);
        setting.setPushNotifications(true);
        setting.setMaximumFileUploadSize(100L);
        setting.setDefaultTheme("Light");
        setting.setMaintenanceMode(false);
    }

    private void recordHistoryAndAudit(PlatformSetting setting, SettingAction action) {
        saveHistory(setting, action);

        PlatformSettingsAuditIntegration.AuditEvent event =
                new PlatformSettingsAuditIntegration.AuditEvent(
                        currentUserProvider.getUserId(),
                        currentUserProvider.getUserName(),
                        action.name(),
                        MODULE,
                        ENTITY_TYPE,
                        setting.getId(),
                        SUCCESS,
                        setting.getVersionNumber(),
                        now(),
                        currentUserProvider.getIpAddress());

        auditIntegration.record(event);
    }

    private void saveHistory(PlatformSetting setting, SettingAction action) {
        PlatformSettingHistory history = PlatformSettingHistory.builder()
                .setting(setting)
                .versionNumber(setting.getVersionNumber())
                .action(action)
                .activityStatus(SUCCESS)
                .settingName(setting.getSettingName())
                .category(setting.getCategory())
                .description(setting.getDescription())
                .status(setting.getStatus())
                .defaultLanguage(setting.getDefaultLanguage())
                .defaultTimeZone(setting.getDefaultTimeZone())
                .defaultCurrency(setting.getDefaultCurrency())
                .dateFormat(setting.getDateFormat())
                .timeFormat(setting.getTimeFormat())
                .numberFormat(setting.getNumberFormat())
                .sessionTimeout(setting.getSessionTimeout())
                .autoLogout(setting.getAutoLogout())
                .passwordExpiry(setting.getPasswordExpiry())
                .maximumLoginAttempts(setting.getMaximumLoginAttempts())
                .maintenanceNotification(setting.getMaintenanceNotification())
                .systemAnnouncement(setting.getSystemAnnouncement())
                .multiFactorAuthentication(setting.getMultiFactorAuthentication())
                .emailNotifications(setting.getEmailNotifications())
                .smsNotifications(setting.getSmsNotifications())
                .pushNotifications(setting.getPushNotifications())
                .maximumFileUploadSize(setting.getMaximumFileUploadSize())
                .defaultTheme(setting.getDefaultTheme())
                .maintenanceMode(setting.getMaintenanceMode())
                .userId(currentUserProvider.getUserId())
                .userName(currentUserProvider.getUserName())
                .ipAddress(currentUserProvider.getIpAddress())
                .changedAt(now())
                .build();

        historyRepository.save(history);
    }

    private PlatformSettingsHistoryResponse toHistoryResponse(PlatformSettingHistory history) {
        return PlatformSettingsHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .settingId(history.getSetting().getId())
                .versionNumber(history.getVersionNumber())
                .action(history.getAction())
                .activityStatus(history.getActivityStatus())
                .settingName(history.getSettingName())
                .category(history.getCategory())
                .description(history.getDescription())
                .status(history.getStatus())
                .defaultLanguage(history.getDefaultLanguage())
                .defaultTimeZone(history.getDefaultTimeZone())
                .defaultCurrency(history.getDefaultCurrency())
                .dateFormat(history.getDateFormat())
                .timeFormat(history.getTimeFormat())
                .numberFormat(history.getNumberFormat())
                .sessionTimeout(history.getSessionTimeout())
                .autoLogout(history.getAutoLogout())
                .passwordExpiry(history.getPasswordExpiry())
                .maximumLoginAttempts(history.getMaximumLoginAttempts())
                .maintenanceNotification(history.getMaintenanceNotification())
                .systemAnnouncement(history.getSystemAnnouncement())
                .multiFactorAuthentication(history.getMultiFactorAuthentication())
                .emailNotifications(history.getEmailNotifications())
                .smsNotifications(history.getSmsNotifications())
                .pushNotifications(history.getPushNotifications())
                .maximumFileUploadSize(history.getMaximumFileUploadSize())
                .defaultTheme(history.getDefaultTheme())
                .maintenanceMode(history.getMaintenanceMode())
                .userId(history.getUserId())
                .userName(history.getUserName())
                .ipAddress(history.getIpAddress())
                .changedAt(history.getChangedAt())
                .build();
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

}