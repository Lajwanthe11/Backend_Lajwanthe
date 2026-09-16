package com.platformadmin.superadmin.globaldashboard.exception;

import java.time.Instant;

public record ApiErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String traceId,
        String path
) {
}