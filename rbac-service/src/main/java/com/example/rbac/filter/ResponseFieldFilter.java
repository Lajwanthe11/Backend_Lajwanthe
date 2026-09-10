package com.example.rbac.filter;

import com.example.rbac.context.DataPermissionContext;
import com.example.rbac.entity.DataAccessRule;
import com.example.rbac.evaluator.FieldLevelRuleEvaluator;
import com.example.rbac.evaluator.FieldLevelRuleEvaluator.FieldEvaluationResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ResponseFieldFilter {

    private final FieldLevelRuleEvaluator fieldLevelRuleEvaluator;
    private final ObjectMapper objectMapper;

    /**
     * Filters a map representing an entity/DTO, removing any fields that are denied or not in allowed list.
     * Denied fields are completely removed (not sent as null, but missing from JSON).
     */
    public Map<String, Object> filterFields(Map<String, Object> data, List<DataAccessRule> rules, DataPermissionContext context) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        if (context != null && (context.isSuperAdmin() || context.isSystemRole())) {
            return new LinkedHashMap<>(data);
        }

        FieldEvaluationResult result = fieldLevelRuleEvaluator.evaluate(rules, context);
        if (result.allFieldsAllowed()) {
            return new LinkedHashMap<>(data);
        }

        Map<String, Object> filtered = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();

            // Check if denied
            boolean isDenied = result.deniedFields().stream()
                    .anyMatch(d -> d.equalsIgnoreCase(key));
            if (isDenied) {
                continue; // completely omit from JSON
            }

            // Check if restricted by allowed list
            if (!result.allowedFields().isEmpty()) {
                boolean isAllowed = result.allowedFields().stream()
                        .anyMatch(a -> a.equalsIgnoreCase(key));
                if (!isAllowed) {
                    continue; // completely omit from JSON
                }
            }

            // If entry value is a nested map, filter recursively
            if (entry.getValue() instanceof Map<?, ?> nestedMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedMap = (Map<String, Object>) nestedMap;
                filtered.put(key, filterFields(typedMap, rules, context));
            } else if (entry.getValue() instanceof List<?> list) {
                filtered.put(key, filterList(list, rules, context));
            } else {
                filtered.put(key, entry.getValue());
            }
        }

        return filtered;
    }

    // Filters a generic Object/DTO by converting to Map, stripping denied fields, and returning filtered Map.
    public Map<String, Object> filterObject(Object object, List<DataAccessRule> rules, DataPermissionContext context) {
        if (object == null) {
            return Collections.emptyMap();
        }

        if (object instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) map;
            return filterFields(typed, rules, context);
        }

        Map<String, Object> map = objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        return filterFields(map, rules, context);
    }

    // Filters a list of records/maps.
    public List<Object> filterList(List<?> items, List<DataAccessRule> rules, DataPermissionContext context) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object> result = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) map;
                result.add(filterFields(typed, rules, context));
            } else if (item != null) {
                result.add(filterObject(item, rules, context));
            }
        }
        return result;
    }

  
     // Masks sensitive field values with '***' instead of excluding them (optional utility).
     
    public Map<String, Object> maskFields(Map<String, Object> data, Set<String> fieldsToMask) {
        if (data == null || data.isEmpty() || fieldsToMask == null || fieldsToMask.isEmpty()) {
            return data;
        }

        Map<String, Object> masked = new LinkedHashMap<>(data);
        for (String field : fieldsToMask) {
            for (String key : masked.keySet()) {
                if (key.equalsIgnoreCase(field)) {
                    masked.put(key, "***");
                }
            }
        }
        return masked;
    }
}
