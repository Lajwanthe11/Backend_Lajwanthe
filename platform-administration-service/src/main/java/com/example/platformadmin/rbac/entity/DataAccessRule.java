package com.example.platformadmin.rbac.entity;

import com.example.platformadmin.rbac.enums.ConditionOperator;
import com.example.platformadmin.rbac.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "data_access_rules",
    indexes = {
        @Index(name = "idx_data_rule_tenant", columnList = "tenant_id"),
        @Index(name = "idx_data_rule_role", columnList = "role_id"),
        @Index(name = "idx_data_rule_resource", columnList = "resource_type"),
        @Index(name = "idx_data_rule_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataAccessRule {

    @Id
    @GeneratedValue
    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 20)
    private RuleType ruleType;

    @Column(name = "condition_field", length = 100)
    private String conditionField;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_operator", length = 20)
    private ConditionOperator conditionOperator;

    @Column(name = "condition_value", columnDefinition = "TEXT")
    private String conditionValue;

    @Column(name = "allowed_fields")
    private String[] allowedFields;

    @Column(name = "denied_fields")
    private String[] deniedFields;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}