package com.example.platformadmin.rbac.evaluator;

import com.example.platformadmin.rbac.entity.DataAccessRule;
import com.example.platformadmin.rbac.enums.ConditionOperator;
import com.example.platformadmin.rbac.enums.RuleType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataAccessSpecificationBuilder {

    private final DynamicTokenResolver tokenResolver;

    // Builds a JPA Specification enforcing row-level security as SQL WHERE clauses.
    public <T> Specification<T> buildSpecification(List<DataAccessRule> rules, DataPermissionContext context) {
        if (context == null || context.isSuperAdmin() || context.isSystemRole()) {
            return (root, query, cb) -> cb.conjunction();
        }

        if (rules == null || rules.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        List<DataAccessRule> rowRules = rules.stream()
                .filter(r -> r.getRuleType() == RuleType.ROW_LEVEL)
                .filter(r -> Boolean.TRUE.equals(r.getActive()))
                .toList();

        if (rowRules.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (DataAccessRule rule : rowRules) {
                Predicate p = buildPredicateForRule(rule, root, cb, context);
                if (p != null) {
                    predicates.add(p);
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private <T> Predicate buildPredicateForRule(DataAccessRule rule, Root<T> root, CriteriaBuilder cb, DataPermissionContext context) {
        if (rule.getConditionOperator() == null) {
            return null;
        }

        String field = rule.getConditionField();
        ConditionOperator op = rule.getConditionOperator();
        String resolvedValue = tokenResolver.resolve(rule.getConditionValue(), context);

        return switch (op) {
            case EQUALS -> {
                if (field == null) yield null;
                Path<Object> path = getPath(root, field);
                yield cb.equal(path.as(String.class), resolvedValue);
            }
            case IN -> {
                if (field == null || resolvedValue == null) yield null;
                jakarta.persistence.criteria.Expression<String> expr = getPath(root, field).as(String.class);
                List<String> values = Arrays.stream(resolvedValue.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                yield expr.in(values);
            }
            case OWNED_BY_USER -> {
                String effectiveField = (field != null && !field.isBlank()) ? field : "createdBy";
                Path<Object> path = getPath(root, effectiveField);
                if (context.getUserId() == null) {
                    yield cb.disjunction();
                }
                yield cb.equal(path.as(String.class), context.getUserId().toString());
            }
            case OWN_DEPT -> {
                String effectiveField = (field != null && !field.isBlank()) ? field : "departmentId";
                Path<Object> path = getPath(root, effectiveField);
                if (context.getDepartmentId() == null) {
                    yield cb.disjunction();
                }
                yield cb.equal(path.as(String.class), context.getDepartmentId());
            }
            case OWN_BRANCH -> {
                String effectiveField = (field != null && !field.isBlank()) ? field : "branchId";
                Path<Object> path = getPath(root, effectiveField);
                if (context.getBranchId() == null) {
                    yield cb.disjunction();
                }
                yield cb.equal(path.as(String.class), context.getBranchId());
            }
        };
    }

    private <T> Path<Object> getPath(Root<T> root, String fieldName) {
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.");
            Path<Object> path = root.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                path = path.get(parts[i]);
            }
            return path;
        }
        return root.get(fieldName);
    }
}
