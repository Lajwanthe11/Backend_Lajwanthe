package com.example.rbac.service;

import com.example.rbac.context.DataPermissionContext;
import com.example.rbac.dto.DataAccessRuleResponse;
import com.example.rbac.dto.RuleTestRequest;
import com.example.rbac.dto.RuleTestResponse;
import com.example.rbac.entity.DataAccessRule;
import com.example.rbac.evaluator.FieldLevelRuleEvaluator;
import com.example.rbac.evaluator.FieldLevelRuleEvaluator.FieldEvaluationResult;
import com.example.rbac.evaluator.RowLevelRuleEvaluator;
import com.example.rbac.evaluator.RulePriorityEvaluator;
import com.example.rbac.filter.ResponseFieldFilter;
import com.example.rbac.repository.DataAccessRuleRepository;
import com.example.rbac.specification.DataAccessSpecificationBuilder;
import com.example.common.tenant.TenantContext;
import com.example.common.security.user.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataPermissionEvaluationService {

    private final DataAccessRuleRepository ruleRepository;
    private final RowLevelRuleEvaluator rowLevelEvaluator;
    private final FieldLevelRuleEvaluator fieldLevelEvaluator;
    private final RulePriorityEvaluator priorityEvaluator;
    private final DataAccessSpecificationBuilder specificationBuilder;
    private final ResponseFieldFilter responseFieldFilter;

    // Builds a DataPermissionContext from current ThreadLocal tenant and security context.
    public DataPermissionContext buildCurrentContext() {
        String tenantStr = TenantContext.getTenantId();
        UUID tenantId = parseUUID(tenantStr);

        UUID userId = null;
        boolean isSuperAdmin = false;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtUserPrincipal jwtUser) {
            userId = parseUUID(jwtUser.getUsername());
            isSuperAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()) ||
                                   "SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()));
        } else if (auth != null && auth.getName() != null) {
            userId = parseUUID(auth.getName());
        }

        return DataPermissionContext.builder()
                .userId(userId)
                .tenantId(tenantId)
                .superAdmin(isSuperAdmin)
                .build();
    }

        // Builds a JPA Specification for the given resource type to apply row-level filters at DB query level.
    public <T> Specification<T> getSpecificationForResource(String resourceType, DataPermissionContext context) {
        if (context == null) {
            context = buildCurrentContext();
        }

        if (context.isSuperAdmin() || context.isSystemRole()) {
            return (root, query, cb) -> cb.conjunction();
        }

        List<DataAccessRule> activeRules = getActiveRulesForContext(resourceType, context);
        return specificationBuilder.buildSpecification(activeRules, context);
    }

    // Evaluates whether a given data row is accessible by the current user context.
    public boolean isRowAccessible(String resourceType, Map<String, Object> rowData, DataPermissionContext context) {
        if (context == null) {
            context = buildCurrentContext();
        }

        if (context.isSuperAdmin() || context.isSystemRole()) {
            return true;
        }

        List<DataAccessRule> activeRules = getActiveRulesForContext(resourceType, context);
        return rowLevelEvaluator.evaluate(activeRules, rowData, context);
    }

    // Filters response map by excluding denied fields and non-allowed fields.
    public Map<String, Object> filterResponseData(String resourceType, Map<String, Object> rowData, DataPermissionContext context) {
        if (context == null) {
            context = buildCurrentContext();
        }

        if (context.isSuperAdmin() || context.isSystemRole()) {
            return rowData;
        }

        List<DataAccessRule> activeRules = getActiveRulesForContext(resourceType, context);
        return responseFieldFilter.filterFields(rowData, activeRules, context);
    }

    // Gets effective data access summary for the current user (used by GET /api/v1/data-permissions/apply).
    public Map<String, Object> getEffectivePermissionsSummary(DataPermissionContext context) {
        if (context == null) {
            context = buildCurrentContext();
        }

        UUID tenantId = context.getTenantId();
        List<DataAccessRule> rules;
        if (tenantId != null) {
            rules = ruleRepository.findByTenantIdAndResourceTypeAndActiveTrueOrderByPriorityDesc(tenantId, "ALL");
            if (rules.isEmpty()) {
                rules = ruleRepository.findAll().stream()
                        .filter(r -> r.getTenantId().equals(tenantId) && Boolean.TRUE.equals(r.getActive()))
                        .toList();
            }
        } else {
            rules = Collections.emptyList();
        }

        List<DataAccessRule> sortedRules = priorityEvaluator.sortByPriority(rules);
        FieldEvaluationResult fieldResult = fieldLevelEvaluator.evaluate(sortedRules, context);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", context.getUserId());
        response.put("tenantId", context.getTenantId());
        response.put("isSuperAdmin", context.isSuperAdmin());
        response.put("isSystemRole", context.isSystemRole());
        response.put("allowedFields", fieldResult.allowedFields());
        response.put("deniedFields", fieldResult.deniedFields());
        response.put("allFieldsAllowed", fieldResult.allFieldsAllowed());
        response.put("activeRulesCount", sortedRules.size());

        return response;
    }

    // Simulates rule evaluation for admin testing without modifying any data.
    public RuleTestResponse simulateRuleTest(RuleTestRequest request) {
        String tenantStr = TenantContext.getTenantId();
        UUID tenantId = parseUUID(tenantStr);

        DataPermissionContext context = DataPermissionContext.builder()
                .userId(request.userId())
                .tenantId(tenantId)
                .roleIds(request.roleId() != null ? Set.of(request.roleId()) : Collections.emptySet())
                .superAdmin(false)
                .systemRole(false)
                .build();

        if (request.userContext() != null) {
            if (request.userContext().get("departmentId") != null) {
                context.setDepartmentId(request.userContext().get("departmentId").toString());
            }
            if (request.userContext().get("branchId") != null) {
                context.setBranchId(request.userContext().get("branchId").toString());
            }
            if (request.userContext().get("organizationId") != null) {
                context.setOrganizationId(request.userContext().get("organizationId").toString());
            }
            context.setAttributes(new HashMap<>(request.userContext()));
        }

        List<DataAccessRule> applicableRules;
        if (request.roleId() != null) {
            applicableRules = ruleRepository.findByTenantIdAndRoleIdAndResourceTypeAndActiveTrueOrderByPriorityDesc(
                    tenantId != null ? tenantId : UUID.randomUUID(),
                    request.roleId(),
                    request.resourceType()
            );
        } else {
            applicableRules = ruleRepository.findByTenantIdAndResourceTypeAndActiveTrueOrderByPriorityDesc(
                    tenantId != null ? tenantId : UUID.randomUUID(),
                    request.resourceType()
            );
        }

        List<DataAccessRule> sortedRules = priorityEvaluator.sortByPriority(applicableRules);

        Map<String, Object> sampleData = request.sampleData() != null ? request.sampleData() : Collections.emptyMap();
        boolean rowAllowed = rowLevelEvaluator.evaluate(sortedRules, sampleData, context);
        String denyReason = rowAllowed ? null : "Row access denied by data access rule condition";

        FieldEvaluationResult fieldResult = fieldLevelEvaluator.evaluate(sortedRules, context);
        Map<String, Object> filteredData = rowAllowed ? responseFieldFilter.filterFields(sampleData, sortedRules, context) : Collections.emptyMap();

        List<DataAccessRuleResponse> matchedRuleDtos = sortedRules.stream()
                .map(this::mapToDto)
                .toList();

        return new RuleTestResponse(
                rowAllowed,
                denyReason,
                fieldResult.allowedFields().toArray(new String[0]),
                fieldResult.deniedFields().toArray(new String[0]),
                filteredData,
                matchedRuleDtos
        );
    }

    private List<DataAccessRule> getActiveRulesForContext(String resourceType, DataPermissionContext context) {
        if (context.getTenantId() == null) {
            return Collections.emptyList();
        }

        List<DataAccessRule> rules;
        if (context.getRoleIds() != null && !context.getRoleIds().isEmpty()) {
            rules = new ArrayList<>();
            for (UUID roleId : context.getRoleIds()) {
                rules.addAll(ruleRepository.findByTenantIdAndRoleIdAndResourceTypeAndActiveTrueOrderByPriorityDesc(
                        context.getTenantId(), roleId, resourceType));
            }
        } else {
            rules = ruleRepository.findByTenantIdAndResourceTypeAndActiveTrueOrderByPriorityDesc(
                    context.getTenantId(), resourceType);
        }

        return priorityEvaluator.sortByPriority(rules);
    }

    private DataAccessRuleResponse mapToDto(DataAccessRule rule) {
        return new DataAccessRuleResponse(
                rule.getRuleId(),
                rule.getTenantId(),
                rule.getRoleId(),
                rule.getResourceType(),
                rule.getRuleType(),
                rule.getConditionField(),
                rule.getConditionOperator(),
                rule.getConditionValue(),
                rule.getAllowedFields(),
                rule.getDeniedFields(),
                rule.getActive(),
                rule.getPriority(),
                rule.getCreatedBy(),
                rule.getCreatedAt()
        );
    }

    private UUID parseUUID(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(str.getBytes());
        }
    }
}
