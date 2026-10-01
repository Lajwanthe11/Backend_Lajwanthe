package com.example.platformadmin.organizations.costcenter.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cost_centers")
public class CostCenter extends BaseEntity {

    @Column(name = "cost_center_code", nullable = false, unique = true, length = 50)
    private String costCenterCode;

    @Column(name = "cost_center_name", nullable = false, length = 150)
    private String costCenterName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CostCenterStatus status = CostCenterStatus.ACTIVE;
}
