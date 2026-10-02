package com.example.platformadmin.organizations.costcenter.dto;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO used to create or update a Cost Center.
 *
 * <p>Contains the Cost Center's business details, organizational
 * relationships, status, and financial information.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class CostCenterRequestDTO {

    @NotBlank(message = "Cost center code is required")
    @Size(max = 50, message = "Cost center code must not exceed 50 characters")
    private String costCenterCode;

    @NotBlank(message = "Cost center name is required")
    @Size(max = 150, message = "Cost center name must not exceed 150 characters")
    private String costCenterName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Cost center status is required")
    private CostCenterStatus status;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.00", message = "Budget cannot be negative")
    private BigDecimal budget;

    @NotNull(message = "Allocated funds is required")
    @DecimalMin(value = "0.00", message = "Allocated funds cannot be negative")
    private BigDecimal allocatedFunds;

    @NotNull(message = "Departmental expenses is required")
    @DecimalMin(value = "0.00", message = "Departmental expenses cannot be negative")
    private BigDecimal departmentalExpenses;
}