package com.example.platformadmin.superadmin.platform_settings_service.exception;

public class SettingNotFoundException extends RuntimeException {

    public SettingNotFoundException(String message) {
        super(message);
    }

}