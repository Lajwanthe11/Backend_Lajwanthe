package com.example.rbac.evaluator;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DynamicTokenResolver {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_.]+)\\}");

    // Resolves dynamic tokens (e.g., '{user.departmentId}', '{user.id}')
    // within the given template string using the provided DataPermissionContext.
    public String resolve(String conditionValue, DataPermissionContext context) {
        if (conditionValue == null || conditionValue.isBlank() || context == null) {
            return conditionValue;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(conditionValue);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String token = matcher.group(1);
            String resolvedValue = resolveTokenValue(token, context);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedValue != null ? resolvedValue : ""));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    // Check if a string contains any dynamic tokens.
    public boolean containsTokens(String value) {
        if (value == null) {
            return false;
        }
        return TOKEN_PATTERN.matcher(value).find();
    }

    private String resolveTokenValue(String token, DataPermissionContext context) {
        return switch (token.toLowerCase()) {
            case "user.id", "user.userid" -> context.getUserId() != null ? context.getUserId().toString() : "";
            case "user.tenantid" -> context.getTenantId() != null ? context.getTenantId().toString() : "";
            case "user.departmentid", "user.deptid" -> context.getDepartmentId() != null ? context.getDepartmentId() : "";
            case "user.branchid" -> context.getBranchId() != null ? context.getBranchId() : "";
            case "user.organizationid", "user.orgid" -> context.getOrganizationId() != null ? context.getOrganizationId() : "";
            default -> {
                // Check custom attributes
                if (token.startsWith("user.")) {
                    String attrKey = token.substring(5);
                    Object val = context.getAttribute(attrKey);
                    yield val != null ? val.toString() : "";
                }
                Object val = context.getAttribute(token);
                yield val != null ? val.toString() : "";
            }
        };
    }
}
