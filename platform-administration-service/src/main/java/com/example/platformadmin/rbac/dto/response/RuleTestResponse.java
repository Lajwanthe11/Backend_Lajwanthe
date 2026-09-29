package com.example.platformadmin.rbac.dto.response;

import java.util.List;
import java.util.Map;

public record RuleTestResponse(
        boolean rowAccessAllowed,
        String rowDenyReason,
        String[] allowedFields,
        String[] deniedFields,
        Map<String, Object> filteredData,
        List<DataAccessRuleResponse> appliedRules
) {
}
