package com.example.auth.devicemanagement.device.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class DeviceStateException extends AppException {

    public DeviceStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}