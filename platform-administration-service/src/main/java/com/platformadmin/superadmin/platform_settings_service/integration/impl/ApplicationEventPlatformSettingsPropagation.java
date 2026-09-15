package com.example.platformadmin.superadmin.platform_settings_service.integration.impl;

import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSetting;
import com.example.platformadmin.superadmin.platform_settings_service.integration.PlatformSettingsPropagationIntegration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default in-process propagation adapter for the super-admin microservice.
 * A future shared/runtime propagation client can replace this adapter without
 * changing the Platform Settings service layer.
 */
@Slf4j
@Component
public class ApplicationEventPlatformSettingsPropagation implements PlatformSettingsPropagationIntegration {

    private final ApplicationEventPublisher eventPublisher;

    // Constructor for ApplicationEventPlatformSettingsPropagation that initializes the event publisher.
    public ApplicationEventPlatformSettingsPropagation(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    // Propagates the platform setting update by publishing an event with the setting ID and version number.
    @Override
    public void propagate(PlatformSetting setting) {

        eventPublisher.publishEvent(new PlatformSettingsUpdatedEvent(
                setting.getId(), setting.getVersionNumber()));

        log.info("Published platform settings update. settingId={}, version={}",
                setting.getId(), setting.getVersionNumber());
    }

    public record PlatformSettingsUpdatedEvent(UUID settingId, Long versionNumber) {
    }

}