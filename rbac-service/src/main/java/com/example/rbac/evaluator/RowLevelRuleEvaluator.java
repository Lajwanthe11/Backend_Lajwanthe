package com.example.rbac.evaluator;

import com.example.rbac.entity.DataAccessRule;
import com.example.rbac.enums.ConditionOperator;
import com.example.rbac.enums.RuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RowLevelRuleEvaluator {

    private final DynamicTokenResolver tokenResolver;

    /**
     * Evaluates whether a user context has access to a given data row based on row-level rules.
     * Deny takes precedence: if any applicable row-level rule condition is violated, access is denied.
     */
    public boolean evaluate(List<DataAccessRule> rules, Map<String, Object> rowData, DataPermissionContext context) {
        if (context == null || context.isSuperAdmin() || context.isSystemRole()) {
            return true;
        }

        if (rules == null || rules.isEmpty() || rowData == null) {
            return true;
        }

        List<DataAccessRule> rowRules = rules.stream()
                .filter(r -> r.getRuleType() == RuleType.ROW_LEVEL)
                .filter(r -> Boolean.TRUE.equals(r.getActive()))
                .toList();

        if (rowRules.isEmpty()) {
            return true;
        }

        for (DataAccessRule rule : rowRules) {
            if (!matchesCondition(rule, rowData, context)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Evaluates a single row-level rule against the provided row data.
     */
    public boolean matchesCondition(DataAccessRule rule, Map<String, Object> rowData, DataPermissionContext context) {
        if (rule.getConditionOperator() == null) {
            return true;
        }

        String fieldName = rule.getConditionField();
        ConditionOperator operator = rule.getConditionOperator();
        String resolvedConditionValue = tokenResolver.resolve(rule.getConditionValue(), context);

        return switch (operator) {
            case EQUALS -> evaluateEquals(fieldName, resolvedConditionValue, rowData);
            case IN -> evaluateIn(fieldName, resolvedConditionValue, rowData);
            case OWNED_BY_USER -> evaluateOwnedByUser(fieldName, rowData, context);
            case OWN_DEPT -> evaluateOwnDept(fieldName, rowData, context);
            case OWN_BRANCH -> evaluateOwnBranch(fieldName, rowData, context);
        };
    }

    private boolean evaluateEquals(String fieldName, String expectedValue, Map<String, Object> rowData) {
        if (fieldName == null) {
            return false;
        }
        Object actualValue = findFieldValue(fieldName, rowData);
        if (actualValue == null) {
            return expectedValue == null || expectedValue.isBlank() || "null".equalsIgnoreCase(expectedValue);
        }
        return Objects.toString(actualValue).equalsIgnoreCase(expectedValue);
    }

    private boolean evaluateIn(String fieldName, String commaSeparatedValues, Map<String, Object> rowData) {
        if (fieldName == null || commaSeparatedValues == null) {
            return false;
        }
        Object actualValue = findFieldValue(fieldName, rowData);
        if (actualValue == null) {
            return false;
        }
        String actualStr = Objects.toString(actualValue).trim();
        Set<String> allowedSet = Arrays.stream(commaSeparatedValues.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        return allowedSet.contains(actualStr);
    }

    private boolean evaluateOwnedByUser(String fieldName, Map<String, Object> rowData, DataPermissionContext context) {
        if (context.getUserId() == null) {
            return false;
        }
        String effectiveField = (fieldName != null && !fieldName.isBlank()) ? fieldName : "userId";
        Object actualValue = findFieldValue(effectiveField, rowData);
        if (actualValue == null) {
            // fallback to createdBy or ownerId if default field not found
            actualValue = findFieldValue("createdBy", rowData);
            if (actualValue == null) {
                actualValue = findFieldValue("ownerId", rowData);
            }
        }
        return actualValue != null && context.getUserId().toString().equalsIgnoreCase(Objects.toString(actualValue));
    }

    private boolean evaluateOwnDept(String fieldName, Map<String, Object> rowData, DataPermissionContext context) {
        if (context.getDepartmentId() == null || context.getDepartmentId().isBlank()) {
            return false;
        }
        String effectiveField = (fieldName != null && !fieldName.isBlank()) ? fieldName : "departmentId";
        Object actualValue = findFieldValue(effectiveField, rowData);
        if (actualValue == null) {
            actualValue = findFieldValue("deptId", rowData);
        }
        return actualValue != null && context.getDepartmentId().equalsIgnoreCase(Objects.toString(actualValue));
    }

    private boolean evaluateOwnBranch(String fieldName, Map<String, Object> rowData, DataPermissionContext context) {
        if (context.getBranchId() == null || context.getBranchId().isBlank()) {
            return false;
        }
        String effectiveField = (fieldName != null && !fieldName.isBlank()) ? fieldName : "branchId";
        Object actualValue = findFieldValue(effectiveField, rowData);
        return actualValue != null && context.getBranchId().equalsIgnoreCase(Objects.toString(actualValue));
    }

    private Object findFieldValue(String key, Map<String, Object> rowData) {
        if (rowData.containsKey(key)) {
            return rowData.get(key);
        }
        // Try case-insensitive lookup
        for (Map.Entry<String, Object> entry : rowData.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key) ||
                entry.getKey().replace("_", "").equalsIgnoreCase(key.replace("_", ""))) {
                return entry.getValue();
            }
        }
        return null;
    }
}
