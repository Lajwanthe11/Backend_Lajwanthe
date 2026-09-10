package com.enterprise.superadmin.platform_health_service.service;



import com.enterprise.superadmin.platform_health_service.HealthStatus;
import com.enterprise.superadmin.platform_health_service.dto.response.PlatformHealthResponse;
import com.enterprise.superadmin.platform_health_service.dto.response.ServiceHealthResponse;
import com.enterprise.superadmin.platform_health_service.integration.ServiceHealthClient;
import com.enterprise.superadmin.platform_health_service.integration.ServiceHealthResult;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for aggregating the health status of all services in the platform.
 */

@Service
public class PlatformHealthService {

    private final ServiceHealthService serviceHealthService;
    private final HealthAggregationService healthAggregationService;
    private final ServiceHealthClient serviceHealthClient;
    private final DiscoveryClient discoveryClient;

    public PlatformHealthService(
            ServiceHealthService serviceHealthService,
            HealthAggregationService healthAggregationService,
            ServiceHealthClient serviceHealthClient,
            DiscoveryClient discoveryClient) {

        this.serviceHealthService = serviceHealthService;
        this.healthAggregationService = healthAggregationService;
        this.serviceHealthClient = serviceHealthClient;
        this.discoveryClient = discoveryClient;
    }

    public PlatformHealthResponse getPlatformHealth() {

        List<ServiceHealthResponse> services =
                getAllServicesHealth();

        if (services == null || services.isEmpty()) {
            return PlatformHealthResponse.builder()
                    .status(HealthStatus.DEGRADED.name())
                    .availability(false)
                    .retrievedAt(LocalDateTime.now())
                    .services(services)
                    .build();
        }

        List<ServiceHealthResponse> validatedServices =
                serviceHealthService.validateHealthResponses(services);

        List<HealthStatus> statuses = validatedServices.stream()
                .map(service ->
                        HealthStatus.valueOf(
                                service.getStatus().trim().toUpperCase()))
                .toList();

        HealthStatus overallStatus =
                healthAggregationService.determineOverallStatus(statuses);

        boolean availability =
                overallStatus == HealthStatus.HEALTHY;

        return PlatformHealthResponse.builder()
                .status(overallStatus.name())
                .availability(availability)
                .retrievedAt(LocalDateTime.now())
                .services(validatedServices)
                .build();
    }

    public List<ServiceHealthResponse> getAllServicesHealth() {

        List<ServiceHealthResponse> services = new ArrayList<>();

        List<String> serviceNames = discoveryClient.getServices();

        if (serviceNames == null || serviceNames.isEmpty()) {
            return services;
        }

        for (String serviceName : serviceNames) {

            try {
                ServiceHealthResult result =
                        serviceHealthClient.getHealth(serviceName);

                String status = mapStatus(result.status());

                boolean availability =
                        HealthStatus.HEALTHY.name().equals(status);

                services.add(
                        ServiceHealthResponse.builder()
                                .serviceName(result.serviceName())
                                .status(status)
                                .availability(availability)
                                .responseTimeMs(null)
                                .build()
                );

            } catch (Exception exception) {

                services.add(
                        ServiceHealthResponse.builder()
                                .serviceName(serviceName)
                                .status(HealthStatus.DEGRADED.name())
                                .availability(false)
                                .responseTimeMs(null)
                                .build()
                );
            }
        }

        return services;
    }

    public ServiceHealthResponse getServiceHealth(String serviceName) {

        try {
            ServiceHealthResult result =
                    serviceHealthClient.getHealth(serviceName);

            String status = mapStatus(result.status());

            boolean availability =
                    HealthStatus.HEALTHY.name().equals(status);

            return ServiceHealthResponse.builder()
                    .serviceName(result.serviceName())
                    .status(status)
                    .availability(availability)
                    .responseTimeMs(null)
                    .build();

        } catch (Exception exception) {

            return ServiceHealthResponse.builder()
                    .serviceName(serviceName)
                    .status(HealthStatus.DEGRADED.name())
                    .availability(false)
                    .responseTimeMs(null)
                    .build();
        }
    }

    private String mapStatus(String status) {

        if (status == null || status.isBlank()) {
            return HealthStatus.DEGRADED.name();
        }

        return switch (status.trim().toUpperCase()) {
            case "UP" -> HealthStatus.HEALTHY.name();
            case "DOWN" -> HealthStatus.FAILED.name();
            case "DEGRADED" -> HealthStatus.DEGRADED.name();
            default -> HealthStatus.DEGRADED.name();
        };
    }
}