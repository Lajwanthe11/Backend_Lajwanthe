package com.example.platformadmin.superadmin.platform_settings_service.dto.request;

import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Represents a request to activate or deactivate a setting.
@Data
public class UpdateSettingStatusRequest {

    @NotNull(message = "Status is required")
    private SettingStatus status;

}