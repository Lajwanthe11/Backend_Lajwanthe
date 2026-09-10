package com.enterprise.superadmin.platform_health_service.integration;

public record ServiceHealthResult(
        String serviceName,
        String status,
        String message
) {
}