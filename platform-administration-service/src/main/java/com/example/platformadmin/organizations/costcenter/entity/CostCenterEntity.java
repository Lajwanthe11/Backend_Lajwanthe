package com.example.platformadmin.organizations.costcenter.entity;

import com.example.common.abstracts.BaseEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "cost_centers",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cost_center_code",
                        columnNames = "cost_center_code"
                )
        }
)
public class CostCenterEntity extends BaseEntity {

    @Column(name = "cost_center_code", nullable = false, length = 50)
    private String costCenterCode;

    @Column(name = "cost_center_name", nullable = false, length = 150)
    private String costCenterName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "budget", nullable = false, precision = 19, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(name = "allocated_funds", nullable = false, precision = 19, scale = 2)
    private BigDecimal allocatedFunds = BigDecimal.ZERO;

    @Column(name = "expenses", nullable = false, precision = 19, scale = 2)
    private BigDecimal expenses = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CostCenterStatus status = CostCenterStatus.ACTIVE;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;
}