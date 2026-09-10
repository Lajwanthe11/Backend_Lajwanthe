package com.enterprise.superadmin.platform_health_service.service;

import com.enterprise.superadmin.platform_health_service.HealthStatus;
import com.enterprise.superadmin.platform_health_service.dto.response.ServiceHealthResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceHealthService {

    public ServiceHealthResponse validateHealthResponse(
            ServiceHealthResponse healthResponse) {

        if (healthResponse == null) {
            throw new IllegalArgumentException(
                    "Health response cannot be null");
        }

        if (healthResponse.getServiceName() == null
                || healthResponse.getServiceName().isBlank()) {
            throw new IllegalArgumentException(
                    "Service name cannot be empty");
        }

        if (healthResponse.getStatus() == null
                || healthResponse.getStatus().isBlank()) {
            throw new IllegalArgumentException(
                    "Health status cannot be empty");
        }

        validateStatus(healthResponse.getStatus());

        return healthResponse;
    }

    public List<ServiceHealthResponse> validateHealthResponses(
            List<ServiceHealthResponse> healthResponses) {

        if (healthResponses == null || healthResponses.isEmpty()) {
            throw new IllegalArgumentException(
                    "Health responses cannot be null or empty");
        }

        return healthResponses.stream()
                .map(this::validateHealthResponse)
                .toList();
    }

    private void validateStatus(String status) {

        try {
            HealthStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid health status: " + status);
        }
    }
}