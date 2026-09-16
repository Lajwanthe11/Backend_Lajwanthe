package com.platformadmin.superadmin.globaldashboard.integration.client;

import com.platformadmin.superadmin.globaldashboard.integration.dto.UserSummaryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
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