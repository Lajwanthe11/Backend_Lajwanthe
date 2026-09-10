package com.enterprise.superadmin.platform_health_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActuatorHealthClientTest {

    @Test
    void shouldReturnUnknownWhenServiceIsUnavailable() {

        ServiceDiscoveryClient discoveryClient =
                new ServiceDiscoveryClient(
                        new TestDiscoveryClient()
                );

        ActuatorHealthClient client =
                new ActuatorHealthClient(
                        RestClient.builder(),
                        discoveryClient
                );

        ServiceHealthResult result =
                client.getHealth("missing-service");

        assertEquals("missing-service", result.serviceName());
        assertEquals("UNKNOWN", result.status());
        assertNotNull(result.message());
    }

    private static class TestDiscoveryClient
            implements org.springframework.cloud.client.discovery.DiscoveryClient {

        @Override
        public String description() {
            return "Test Discovery Client";
        }

        @Override
        public List<ServiceInstance> getInstances(String serviceId) {
            return List.of();
        }

        @Override
        public List<String> getServices() {
            return List.of();
        }
    }
}