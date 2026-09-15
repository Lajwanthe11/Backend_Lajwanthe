package com.example.platformadmin.superadmin.superadmindashboard.integration;

import org.springframework.stereotype.Component;

/**
 * TEMPORARY stub - returns mock data so the dashboard pipeline can be built
 * and tested before the Platform Health service is available.
 *
 * TODO: replace with a real RestClient-based implementation once confirmed.
 */
@Component
public class PlatformHealthClientStub implements PlatformHealthClient {

    @Override
    public SystemHealth getSystemHealth() {
        return new SystemHealth(
                "HEALTHY",
                "RUNNING",
                "CONNECTED",
                68.0,
                42.5,
                55.3
        );
    }
}