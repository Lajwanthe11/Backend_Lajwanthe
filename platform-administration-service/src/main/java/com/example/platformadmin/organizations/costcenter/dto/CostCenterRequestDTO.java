package com.example.platformadmin.organizations.costcenter.dto;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CostCenterRequestDTO {

    @NotBlank(message = "Cost center code is required")
    @Size(max = 50, message = "Cost center code must not exceed 50 characters")
    private String costCenterCode;

    @NotBlank(message = "Cost center name is required")
    @Size(max = 150, message = "Cost center name must not exceed 150 characters")
    private String costCenterName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Budget amount cannot be negative")
    private BigDecimal budgetAmount;

    private CostCenterStatus status;
}
