package com.example.platformadmin.organizations.costcenter.entity;

import com.example.common.abstracts.BaseEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cost_centers", schema = "public")
public class CostCenterEntity extends BaseEntity {

    @Column(name = "cost_center_code", nullable = false, unique = true, length = 50)
    private String costCenterCode;

    @Column(name = "cost_center_name", nullable = false, length = 150)
    private String costCenterName;


    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "budget", precision = 19, scale = 2)
    private BigDecimal budget;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CostCenterStatus status = CostCenterStatus.ACTIVE;
}