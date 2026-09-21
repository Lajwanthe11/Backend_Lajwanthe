package com.example.platformadmin.superadmin.platform_health_service.service;

import com.example.platformadmin.superadmin.platform_health_service.HealthStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthAggregationService {

    public HealthStatus determineOverallStatus(List<HealthStatus> statuses) {

        if (statuses == null || statuses.isEmpty()) {
            return HealthStatus.DEGRADED;
        }

        if (statuses.contains(HealthStatus.FAILED)) {
            return HealthStatus.FAILED;
        }

        if (statuses.contains(HealthStatus.DEGRADED)) {
            return HealthStatus.DEGRADED;
        }

        return HealthStatus.HEALTHY;
    }
}