 package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.DataAccessRule;
import com.example.platformadmin.rbac.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DataAccessRuleRepository
        extends JpaRepository<DataAccessRule, UUID> {

    List<DataAccessRule> findByTenantIdAndRoleIdAndResourceTypeAndActiveTrueOrderByPriorityDesc(
            UUID tenantId,
            UUID roleId,
            String resourceType
    );

    List<DataAccessRule> findByTenantIdAndResourceTypeAndActiveTrueOrderByPriorityDesc(
            UUID tenantId,
            String resourceType
    );

    List<DataAccessRule> findByTenantIdAndRoleIdAndActiveTrue(
            UUID tenantId,
            UUID roleId
    );

    List<DataAccessRule> findByTenantIdAndRuleTypeAndActiveTrue(
            UUID tenantId,
            RuleType ruleType
    );

    List<DataAccessRule> findByTenantIdAndActiveTrue(UUID tenantId);

    List<DataAccessRule> findByTenantId(UUID tenantId);
}