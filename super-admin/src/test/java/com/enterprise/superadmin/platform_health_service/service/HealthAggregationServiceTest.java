package com.enterprise.superadmin.platform_health_service.service;

import com.enterprise.superadmin.platform_health_service.HealthStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthAggregationServiceTest {

    private final HealthAggregationService healthAggregationService =
            new HealthAggregationService();

    @Test
    void shouldReturnHealthyWhenAllServicesAreHealthy() {

        List<HealthStatus> statuses = List.of(
                HealthStatus.HEALTHY,
                HealthStatus.HEALTHY
        );

        HealthStatus result =
                healthAggregationService.determineOverallStatus(statuses);

        assertEquals(HealthStatus.HEALTHY, result);
    }

    @Test
    void shouldReturnDegradedWhenAnyServiceIsDegraded() {

        List<HealthStatus> statuses = List.of(
                HealthStatus.HEALTHY,
                HealthStatus.DEGRADED
        );

        HealthStatus result =
                healthAggregationService.determineOverallStatus(statuses);

        assertEquals(HealthStatus.DEGRADED, result);
    }

    @Test
    void shouldReturnFailedWhenAnyServiceHasFailed() {

        List<HealthStatus> statuses = List.of(
                HealthStatus.HEALTHY,
                HealthStatus.FAILED
        );

        HealthStatus result =
                healthAggregationService.determineOverallStatus(statuses);

        assertEquals(HealthStatus.FAILED, result);
    }

    @Test
    void shouldReturnFailedWhenOneServiceIsFailedAndAnotherIsDegraded() {

        List<HealthStatus> statuses = List.of(
                HealthStatus.DEGRADED,
                HealthStatus.FAILED
        );

        HealthStatus result =
                healthAggregationService.determineOverallStatus(statuses);

        assertEquals(HealthStatus.FAILED, result);
    }

    @Test
    void shouldReturnDegradedWhenStatusListIsEmpty() {

        HealthStatus result =
                healthAggregationService.determineOverallStatus(List.of());

        assertEquals(HealthStatus.DEGRADED, result);
    }

    @Test
    void shouldReturnDegradedWhenStatusListIsNull() {

        HealthStatus result =
                healthAggregationService.determineOverallStatus(null);

        assertEquals(HealthStatus.DEGRADED, result);
    }
}