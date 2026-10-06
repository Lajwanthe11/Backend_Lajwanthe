package com.example.platformadmin.organizations.costcenter.DTO;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenterResponseDTO {

    private Long id;

    private String costCenterCode;

    private String costCenterName;

    private String description;

    private CostCenterStatus status;

    private UUID organizationId;

    private Long companyId;

    private Long departmentId;

    private BigDecimal budget;

    private BigDecimal allocatedFunds;

    private BigDecimal departmentalExpenses;

    private Boolean deleted;

    private LocalDate deletedAt;

    private String deletedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}

