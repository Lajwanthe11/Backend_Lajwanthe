package com.enterprise.superadmin.platform_health_service.integration;

public interface ServiceHealthClient {
    ServiceHealthResult getHealth(String serviceName);
}
