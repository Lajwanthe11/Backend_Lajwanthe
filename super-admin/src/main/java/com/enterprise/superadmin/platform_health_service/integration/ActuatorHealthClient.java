package com.enterprise.superadmin.platform_health_service.integration;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ActuatorHealthClient implements ServiceHealthClient {

    private final RestClient restClient;
    private final ServiceDiscoveryClient serviceDiscoveryClient;

    public ActuatorHealthClient(
            RestClient.Builder restClientBuilder,
            ServiceDiscoveryClient serviceDiscoveryClient) {

        this.restClient = restClientBuilder.build();
        this.serviceDiscoveryClient = serviceDiscoveryClient;
    }

    @Override
    public ServiceHealthResult getHealth(String serviceName) {

        try {
            ServiceInstance instance =
                    serviceDiscoveryClient.getServiceInstance(serviceName);

            String healthUrl =
                    instance.getUri() + "/actuator/health";

            ActuatorHealthResponse response = restClient
                    .get()
                    .uri(healthUrl)
                    .retrieve()
                    .body(ActuatorHealthResponse.class);

            if (response == null || response.status() == null) {

                return unknownResult(
                        serviceName,
                        "Invalid health response"
                );
            }

            return new ServiceHealthResult(
                    serviceName,
                    response.status(),
                    "Health retrieved successfully"
            );

        } catch (Exception ex) {

            return unknownResult(
                    serviceName,
                    "Health source unavailable"
            );
        }
    }

    private ServiceHealthResult unknownResult(
            String serviceName,
            String message) {

        return new ServiceHealthResult(
                serviceName,
                "UNKNOWN",
                message
        );
    }

    private record ActuatorHealthResponse(
            String status
    ) {
    }
}