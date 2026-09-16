package com.example.platformadmin.superadmin.platform_settings_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

// Represents a consistent error response returned by the API.
@Getter
@Builder
public class ErrorResponse {

    private OffsetDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private Map<String, String> validationErrors;

}