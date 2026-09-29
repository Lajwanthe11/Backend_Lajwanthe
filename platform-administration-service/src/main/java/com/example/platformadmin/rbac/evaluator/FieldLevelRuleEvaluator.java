package com.example.platformadmin.rbac.evaluator;

import com.example.platformadmin.rbac.entity.DataAccessRule;
import com.example.platformadmin.rbac.enums.RuleType;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FieldLevelRuleEvaluator {

    public record FieldEvaluationResult(
            Set<String> allowedFields,
            Set<String> deniedFields,
            boolean allFieldsAllowed
    ) {}

    /**
     * Evaluates field-level rules with DENY-takes-precedence conflict resolution.
     * If any rule explicitly denies a field, it is denied.
     * If any rule specifies allowed_fields, only those non-denied fields are permitted.
     */
    public FieldEvaluationResult evaluate(List<DataAccessRule> rules, DataPermissionContext context) {
        if (context == null || context.isSuperAdmin() || context.isSystemRole()) {
            return new FieldEvaluationResult(Collections.emptySet(), Collections.emptySet(), true);
        }

        if (rules == null || rules.isEmpty()) {
            return new FieldEvaluationResult(Collections.emptySet(), Collections.emptySet(), true);
        }

        List<DataAccessRule> fieldRules = rules.stream()
                .filter(r -> r.getRuleType() == RuleType.FIELD_LEVEL)
                .filter(r -> Boolean.TRUE.equals(r.getActive()))
                .toList();

        if (fieldRules.isEmpty()) {
            return new FieldEvaluationResult(Collections.emptySet(), Collections.emptySet(), true);
        }

        Set<String> accumulatedDeniedFields = new HashSet<>();
        Set<String> accumulatedAllowedFields = null; // null means all allowed initially
        boolean hasAllowedConstraint = false;

        for (DataAccessRule rule : fieldRules) {
            // Collect denied fields
            if (rule.getDeniedFields() != null && rule.getDeniedFields().length > 0) {
                for (String denied : rule.getDeniedFields()) {
                    if (denied != null && !denied.isBlank()) {
                        accumulatedDeniedFields.add(denied.trim());
                    }
                }
            }

            // Collect allowed fields (most restrictive intersection if multiple rules specify allow-lists)
            if (rule.getAllowedFields() != null && rule.getAllowedFields().length > 0) {
                Set<String> currentRuleAllowed = new HashSet<>();
                for (String allowed : rule.getAllowedFields()) {
                    if (allowed != null && !allowed.isBlank()) {
                        currentRuleAllowed.add(allowed.trim());
                    }
                }

                if (!hasAllowedConstraint) {
                    accumulatedAllowedFields = new HashSet<>(currentRuleAllowed);
                    hasAllowedConstraint = true;
                } else {
                    accumulatedAllowedFields.retainAll(currentRuleAllowed);
                }
            }
        }

        // Apply DENY takes precedence: remove any denied fields from allowed list
        if (accumulatedAllowedFields != null) {
            accumulatedAllowedFields.removeAll(accumulatedDeniedFields);
        }

        return new FieldEvaluationResult(
                accumulatedAllowedFields != null ? accumulatedAllowedFields : Collections.emptySet(),
                accumulatedDeniedFields,
                !hasAllowedConstraint && accumulatedDeniedFields.isEmpty()
        );
    }

    /**
     * Determines whether a specific field name is accessible.
     */
    public boolean isFieldAccessible(String fieldName, List<DataAccessRule> rules, DataPermissionContext context) {
        if (fieldName == null) {
            return false;
        }

        FieldEvaluationResult result = evaluate(rules, context);
        if (result.allFieldsAllowed()) {
            return true;
        }

        if (result.deniedFields().stream().anyMatch(d -> d.equalsIgnoreCase(fieldName))) {
            return false;
        }

        if (!result.allowedFields().isEmpty()) {
            return result.allowedFields().stream().anyMatch(a -> a.equalsIgnoreCase(fieldName));
        }

        return true;
    }
}
