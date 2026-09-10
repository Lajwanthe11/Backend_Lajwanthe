package com.enterprise.superadmin.platform_health_service.dto.response;

import java.time.Instant;

public class ErrorResponse {

    private String code;
    private String message;
    private Instant timestamp;
    private String correlationId;

    public ErrorResponse(
            String code,
            String message,
            Instant timestamp,
            String correlationId) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
