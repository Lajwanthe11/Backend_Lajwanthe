package com.example.platformadmin.rbac.dto.request;

import com.example.platformadmin.rbac.enums.ConditionOperator;
import com.example.platformadmin.rbac.enums.RuleType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateDataAccessRuleRequest(

        @NotNull
        UUID roleId,

        @NotBlank
        @Size(max = 50)
        String resourceType,

        @NotNull
        RuleType ruleType,

        @Size(max = 100)
        String conditionField,

        ConditionOperator conditionOperator,

        String conditionValue,

        String[] allowedFields,

        String[] deniedFields,

        @Min(0)
        Integer priority
) {
}