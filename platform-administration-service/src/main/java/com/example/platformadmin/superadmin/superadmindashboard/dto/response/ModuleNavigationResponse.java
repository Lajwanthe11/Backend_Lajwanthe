package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleNavigationResponse {
    private String code;           // e.g. "CONFIG", "LICENSES", "FEATURES"
    private String title;          // e.g. "Platform Configuration"
    private String description;    // e.g. "Manage system-wide configuration keys & rollbacks"
    private String category;       // e.g. "Platform Settings", "Governance", "Operations"
    private String apiPath;        // e.g. "/api/v1/platform-configurations"
    private String uiRoute;        // e.g. "/admin/configurations"
    private String icon;
}
