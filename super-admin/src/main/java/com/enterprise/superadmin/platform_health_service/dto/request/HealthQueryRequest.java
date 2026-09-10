package com.enterprise.superadmin.platform_health_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public class HealthQueryRequest {

    @NotBlank(message = "Service name must not be blank")
    private String serviceName;

    public HealthQueryRequest() {
    }

    public HealthQueryRequest(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}