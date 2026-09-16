package com.example.platformadmin.superadmin.superadmindashboard.integration;

import java.time.Instant;
import java.util.List;

/**
 * Client for calling the User Management service.
 * Real implementation should use RestClient + Eureka service discovery
 * once the User Management team's APIs are ready. For now, see
 * {@link UserManagementClientStub}.
 */
public interface UserManagementClient {

    UserStatistics getUserStatistics();

    List<LoginActivityRecord> getRecentLoginActivities(int limit);

    record UserStatistics(long totalUsers, long activeUsers, long onlineUsers) {
    }

    record LoginActivityRecord(
            String userId,
            String userName,
            String ipAddress,
            Instant loginTime,
            String status // e.g. "SUCCESS", "FAILED"
    ) {
    }
}