package com.example.rbac.dto.response;

import com.example.rbac.enums.ConditionOperator;
import com.example.rbac.enums.RuleType;

import java.time.LocalDateTime;
import java.util.UUID;

public record DataAccessRuleResponse(

        UUID ruleId,
        UUID tenantId,
        UUID roleId,
        String resourceType,
        RuleType ruleType,
        String conditionField,
        ConditionOperator conditionOperator,
        String conditionValue,
        String[] allowedFields,
        String[] deniedFields,
        Boolean active,
        Integer priority,
        UUID createdBy,
        LocalDateTime createdAt
) {
}