package com.example.platformadmin.superadmin.platform_settings_service.integration;

import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSetting;

/**
 * Contract for the approved platform mechanism used to make activated settings effective.
 * The concrete propagation mechanism is an architecture decision and is therefore pluggable.
 */
public interface PlatformSettingsPropagationIntegration {

    void propagate(PlatformSetting setting);

}