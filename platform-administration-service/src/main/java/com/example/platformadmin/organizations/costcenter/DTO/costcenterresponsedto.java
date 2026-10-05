package com.example.platformadmin.organizations.costcenter.DTO;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CostCenterResponseDTO {

    private Long id;

    private String code;

    private String name;

    private String description;

    private CostCenterStatus status;

    private UUID organizationId;

    private Long companyId;

    private Long departmentId;

    private BigDecimal budget;

    private BigDecimal allocatedFunds;

    private BigDecimal departmentalExpenses;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}