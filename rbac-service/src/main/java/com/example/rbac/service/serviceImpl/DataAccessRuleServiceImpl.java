package com.example.rbac.service.serviceImpl;

import com.example.rbac.dto.*;
import com.example.rbac.entity.DataAccessRule;
import com.example.rbac.enums.RuleType;
import com.example.rbac.exception.DataAccessRuleNotFoundException;
import com.example.rbac.exception.InvalidDataAccessRuleException;
import com.example.rbac.repository.DataAccessRuleRepository;
import com.example.common.security.user.JwtUserPrincipal;
import com.example.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DataAccessRuleServiceImpl implements DataAccessRuleService {

    private final DataAccessRuleRepository repository;
    private final DataPermissionEvaluationService evaluationService;

    @Override
    public DataAccessRuleResponse create(CreateDataAccessRuleRequest request) {
        UUID tenantId = getCurrentTenantId();
        UUID userId = getCurrentUserId();

        validateRule(request);

        DataAccessRule rule = DataAccessRule.builder()
                .tenantId(tenantId)
                .roleId(request.roleId())
                .resourceType(request.resourceType().trim().toUpperCase())
                .ruleType(request.ruleType())
                .conditionField(request.conditionField() != null ? request.conditionField().trim() : null)
                .conditionOperator(request.conditionOperator())
                .conditionValue(request.conditionValue() != null ? request.conditionValue().trim() : null)
                .allowedFields(request.allowedFields())
                .deniedFields(request.deniedFields())
                .priority(request.priority() == null ? 0 : request.priority())
                .active(true)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();

        DataAccessRule saved = repository.save(rule);
        log.info("Created data access rule: ID={}, Role={}, Resource={}, Tenant={}",
                saved.getRuleId(), saved.getRoleId(), saved.getResourceType(), tenantId);

        return toResponse(saved);
    }

    @Override
    public DataAccessRuleResponse update(UUID ruleId, UpdateDataAccessRuleRequest request) {
        UUID tenantId = getCurrentTenantId();

        DataAccessRule rule = repository.findById(ruleId)
                .orElseThrow(() -> new DataAccessRuleNotFoundException(
                        "Data access rule not found: " + ruleId));

        if (!rule.getTenantId().equals(tenantId)) {
            throw new DataAccessRuleNotFoundException(
                    "Data access rule not found: " + ruleId);
        }

        if (request.roleId() != null) {
            rule.setRoleId(request.roleId());
        }

        if (request.resourceType() != null && !request.resourceType().isBlank()) {
            rule.setResourceType(request.resourceType().trim().toUpperCase());
        }

        if (request.ruleType() != null) {
            rule.setRuleType(request.ruleType());
        }

        if (request.conditionField() != null) {
            rule.setConditionField(request.conditionField().trim());
        }

        if (request.conditionOperator() != null) {
            rule.setConditionOperator(request.conditionOperator());
        }

        if (request.conditionValue() != null) {
            rule.setConditionValue(request.conditionValue().trim());
        }

        if (request.allowedFields() != null) {
            rule.setAllowedFields(request.allowedFields());
        }

        if (request.deniedFields() != null) {
            rule.setDeniedFields(request.deniedFields());
        }

        if (request.priority() != null) {
            rule.setPriority(request.priority());
        }

        if (request.active() != null) {
            rule.setActive(request.active());
        }

        DataAccessRule updated = repository.save(rule);
        log.info("Updated data access rule: ID={}, Tenant={}", updated.getRuleId(), tenantId);

        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public DataAccessRuleResponse getById(UUID ruleId) {
        UUID tenantId = getCurrentTenantId();

        DataAccessRule rule = repository.findById(ruleId)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new DataAccessRuleNotFoundException(
                        "Data access rule not found: " + ruleId));

        return toResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataAccessRuleResponse> getRules() {
        UUID tenantId = getCurrentTenantId();

        List<DataAccessRule> rules = repository.findByTenantIdAndActiveTrue(tenantId);
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }

        return rules.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataAccessRuleResponse> getRulesByRole(UUID roleId) {
        UUID tenantId = getCurrentTenantId();

        List<DataAccessRule> rules = repository.findByTenantIdAndRoleIdAndActiveTrue(tenantId, roleId);
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }

        return rules.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID ruleId) {
        UUID tenantId = getCurrentTenantId();

        DataAccessRule rule = repository.findById(ruleId)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new DataAccessRuleNotFoundException(
                        "Data access rule not found: " + ruleId));

        repository.delete(rule);
        log.info("Deleted data access rule: ID={}, Tenant={}", ruleId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public RuleTestResponse testRule(RuleTestRequest request) {
        return evaluationService.simulateRuleTest(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataPermissionReportResponse> getReport() {
        UUID tenantId = getCurrentTenantId();

        List<DataAccessRule> rules = repository.findByTenantId(tenantId);
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }

        // Group by roleId and resourceType
        Map<String, List<DataAccessRule>> grouped = rules.stream()
                .collect(Collectors.groupingBy(r -> r.getRoleId() + "::" + r.getResourceType()));

        List<DataPermissionReportResponse> reports = new ArrayList<>();

        for (Map.Entry<String, List<DataAccessRule>> entry : grouped.entrySet()) {
            List<DataAccessRule> groupRules = entry.getValue();
            if (groupRules.isEmpty())
                continue;

            DataAccessRule sample = groupRules.get(0);
            long rowCount = groupRules.stream().filter(r -> r.getRuleType() == RuleType.ROW_LEVEL).count();
            long fieldCount = groupRules.stream().filter(r -> r.getRuleType() == RuleType.FIELD_LEVEL).count();

            List<DataAccessRuleResponse> ruleResponses = groupRules.stream()
                    .map(this::toResponse)
                    .toList();

            reports.add(new DataPermissionReportResponse(
                    sample.getRoleId(),
                    "Role-" + sample.getRoleId().toString().substring(0, 8),
                    sample.getResourceType(),
                    groupRules.size(),
                    rowCount,
                    fieldCount,
                    ruleResponses));
        }

        return reports;
    }

    private void validateRule(CreateDataAccessRuleRequest request) {
        if (request.roleId() == null) {
            throw new InvalidDataAccessRuleException("Role ID is required");
        }

        if (request.resourceType() == null || request.resourceType().isBlank()) {
            throw new InvalidDataAccessRuleException("Resource type is required");
        }

        if (request.ruleType() == null) {
            throw new InvalidDataAccessRuleException("Rule type is required");
        }

        if (request.ruleType() == RuleType.ROW_LEVEL) {
            if (request.conditionOperator() == null) {
                throw new InvalidDataAccessRuleException("Condition operator is required for ROW_LEVEL rule");
            }
        }

        if (request.ruleType() == RuleType.FIELD_LEVEL) {
            if ((request.allowedFields() == null || request.allowedFields().length == 0)
                    && (request.deniedFields() == null || request.deniedFields().length == 0)) {
                throw new InvalidDataAccessRuleException(
                        "Allowed or denied fields must be provided for FIELD_LEVEL rule");
            }
        }

        if (request.allowedFields() != null && request.deniedFields() != null) {
            for (String denied : request.deniedFields()) {
                for (String allowed : request.allowedFields()) {
                    if (denied.equalsIgnoreCase(allowed)) {
                        throw new InvalidDataAccessRuleException(
                                "A field cannot be both allowed and denied: " + allowed);
                    }
                }
            }
        }
    }

    private DataAccessRuleResponse toResponse(DataAccessRule rule) {
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
                rule.getCreatedAt());
    }

    private UUID getCurrentTenantId() {
        String tenantStr = TenantContext.getTenantId();
        if (tenantStr == null || tenantStr.isBlank()) {
            tenantStr = TenantContext.DEFAULT_TENANT_ID;
        }
        try {
            return UUID.fromString(tenantStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(tenantStr.getBytes());
        }
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof JwtUserPrincipal jwtUser) {
                try {
                    return UUID.fromString(jwtUser.getUsername());
                } catch (IllegalArgumentException e) {
                    return UUID.nameUUIDFromBytes(jwtUser.getUsername().getBytes());
                }
            }
            if (auth.getName() != null && !auth.getName().isBlank()) {
                try {
                    return UUID.fromString(auth.getName());
                } catch (IllegalArgumentException e) {
                    return UUID.nameUUIDFromBytes(auth.getName().getBytes());
                }
            }
        }
        // Fallback for system operations
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}