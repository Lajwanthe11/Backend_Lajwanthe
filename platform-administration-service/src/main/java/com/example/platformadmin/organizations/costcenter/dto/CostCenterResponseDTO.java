package com.example.platformadmin.organizations.costcenter.dto;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CostCenterResponseDTO {

    private Long id;

    private String costCenterCode;

    private String costCenterName;

    private String description;

    private UUID organizationId;

    private Long companyId;

    private Long departmentId;

    private BigDecimal budgetAmount;

    private CostCenterStatus status;

    private String tenantId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    private Long version;
}
