package com.example.platformadmin.rbac.dto.response;

import java.util.List;
import java.util.Map;

// The frontend receives one structured answer instead of hardcoding permission rules.
public record UiPermissionResponse(
        List<String> permissions,
        Map<String, ModuleAccess> modules,
        List<String> menuItems,
        List<String> dashboardWidgets) {
}
