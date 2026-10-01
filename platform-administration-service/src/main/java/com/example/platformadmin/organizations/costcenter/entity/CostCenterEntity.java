package com.example.platformadmin.organizations.costcenter.entity;

import com.example.common.abstracts.BaseEntity;
import com.example.platformadmin.organizations.company.entity.Company;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.department.entity.Department;
import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CostCenterStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "budget", precision = 19, scale = 2, nullable = false)
    private BigDecimal budget;

    @Column(name = "allocated_funds", precision = 19, scale = 2, nullable = false)
    private BigDecimal allocatedFunds;

    @Column(name = "departmental_expenses", precision = 19, scale = 2, nullable = false)
    private BigDecimal departmentalExpenses;
}