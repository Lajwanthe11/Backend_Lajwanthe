package com.example.rbac.evaluator;

import com.example.rbac.entity.DataAccessRule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RulePriorityEvaluator {

    /**
     * Sorts data access rules according to priority in descending order
     * (higher priority number evaluated first).
     */
    public List<DataAccessRule> sortByPriority(List<DataAccessRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return List.of();
        }

        return rules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getActive()))
                .sorted(Comparator.comparingInt((DataAccessRule r) -> r.getPriority() != null ? r.getPriority() : 0).reversed())
                .toList();
    }
}
