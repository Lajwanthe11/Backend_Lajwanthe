package com.example.platformadmin.organizations.costcenter.dto;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenterResponseDTO {

    private UUID id;
    private String costCenterCode;
    private String costCenterName;
    private String description;
    private UUID organizationId;
    private Long companyId;
    private Long departmentId;
    private BigDecimal allocatedBudget;
    private String currency;
    private CostCenterStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}