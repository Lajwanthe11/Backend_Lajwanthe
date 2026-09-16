package com.example.platformadmin.superadmin.globaldashboard.integration.client;

import com.example.platformadmin.superadmin.globaldashboard.integration.dto.UserSummaryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("globalDashboardUserClientStub")
@Profile({"local", "default"})
public class UserManagementClientStub implements UserManagementClient {

    @Override
    public UserSummaryResponse getUserSummary() {
        return new UserSummaryResponse(
                1250,
                1120,
                87
        );
    }
}