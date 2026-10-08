package com.example.platformadmin.rbac.dto.response;

import java.util.List;
import java.util.UUID;

public record DataPermissionReportResponse(
        UUID roleId,
        String roleName,
        String resourceType,
        long totalRules,
        long rowLevelRulesCount,
        long fieldLevelRulesCount,
        List<DataAccessRuleResponse> rules
) {
}
