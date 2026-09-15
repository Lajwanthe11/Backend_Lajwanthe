package com.example.platformadmin.superadmin.platform_health_service.integration;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceDiscoveryClient {

    private final DiscoveryClient discoveryClient;

    public ServiceDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public ServiceInstance getServiceInstance(String serviceName) {

        List<ServiceInstance> instances =
                discoveryClient.getInstances(serviceName);

        if (instances == null || instances.isEmpty()) {
            throw new IllegalStateException(
                    "No available instance found for service: " + serviceName
            );
        }

        return instances.get(0);
    }
}