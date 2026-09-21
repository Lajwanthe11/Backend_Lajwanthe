package com.example.platformadmin.superadmin.platform_health_service.integration;

public interface ServiceHealthClient {
    ServiceHealthResult getHealth(String serviceName);
}
