package com.example.rbac.dto;

import java.util.List;
import java.util.Map;

/**
 * Response body for {@code GET /api/v1/auth/me/permissions}.
 * The frontend must drive all show/hide logic from this response -
 * permissions must never be hardcoded client-side.
 */
public record UiPermissionResponse(
                List<String> permissions,
                Map<String, ModuleAccess> modules,
                List<String> menuItems,
                List<String> dashboardWidgets) {
}
