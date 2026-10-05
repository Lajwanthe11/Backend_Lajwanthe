package com.example.platformadmin.organizations.costcenter.dto;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenterRequestDTO {

    @NotBlank(message = "Cost center code is required")
    @Size(max = 50, message = "Code must be 50 characters or less")
    private String costCenterCode;

    @NotBlank(message = "Cost center name is required")
    @Size(max = 255, message = "Name must be 255 characters or less")
    private String costCenterName;

    @Size(max = 500, message = "Description must be 500 characters or less")
    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private Long companyId;

    private Long departmentId;

    @PositiveOrZero(message = "Allocated budget must be zero or positive")
    @NotNull(message = "Allocated budget is required")
    private BigDecimal allocatedBudget;

    @Pattern(
            regexp = "^(?i)(USD|EUR|GBP|INR|CAD|AUD|JPY|SGD|CHF|AED)$",
            message = "Invalid currency code. Allowed values: USD, EUR, GBP, INR, CAD, AUD, JPY, SGD, CHF, AED"
    )
    private String currency;

    private CostCenterStatus status;
}