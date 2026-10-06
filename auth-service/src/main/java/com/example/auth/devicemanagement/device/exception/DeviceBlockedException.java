package com.example.auth.devicemanagement.device.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class DeviceBlockedException extends AppException {

    public DeviceBlockedException(String deviceIdentifier) {
        super("Access denied: device '" + deviceIdentifier + "' is blocked", HttpStatus.FORBIDDEN);
    }
}