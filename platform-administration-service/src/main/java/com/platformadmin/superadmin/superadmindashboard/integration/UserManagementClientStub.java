package com.example.platformadmin.superadmin.superadmindashboard.integration;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * TEMPORARY stub - returns mock data so the dashboard pipeline can be built
 * and tested before the User Management service is available.
 *
 * TODO: replace with a real RestClient-based implementation once the User
 * Management team confirms their Eureka service ID and API contract.
 */
@Component
public class UserManagementClientStub implements UserManagementClient {

    @Override
    public UserStatistics getUserStatistics() {
        return new UserStatistics(1240, 875, 132);
    }

    @Override
    public List<LoginActivityRecord> getRecentLoginActivities(int limit) {
        List<LoginActivityRecord> mockActivities = List.of(
                new LoginActivityRecord("u-1001", "arjun.rao", "10.0.0.14",
                        Instant.now().minus(5, ChronoUnit.MINUTES), "SUCCESS"),
                new LoginActivityRecord("u-1042", "priya.menon", "10.0.0.22",
                        Instant.now().minus(18, ChronoUnit.MINUTES), "SUCCESS"),
                new LoginActivityRecord("u-1077", "unknown", "203.0.113.5",
                        Instant.now().minus(40, ChronoUnit.MINUTES), "FAILED")
        );
        return mockActivities.stream().limit(limit).toList();
    }
}