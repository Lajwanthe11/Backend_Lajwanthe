package com.example.platformadmin.organizations.costcenter.dto;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class CostCenterRequestDTO {

    @NotBlank(message = "Cost center code is required")
    @Size(
            max = 255,
            message = "Cost center code must not exceed 255 characters"
    )
    private String costCenterCode;

    @NotBlank(message = "Cost center name is required")
    @Size(
            max = 255,
            message = "Cost center name must not exceed 255 characters"
    )
    private String costCenterName;

    @Size(
            max = 500,
            message = "Description must not exceed 500 characters"
    )
    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Budget amount cannot be negative"
    )
    private BigDecimal budgetAmount;

    @Size(
            max = 10,
            message = "Currency must not exceed 10 characters"
    )
    private String currency = "USD";

    private CostCenterStatus status = CostCenterStatus.ACTIVE;

    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public String getCostCenterName() {
        return costCenterName;
    }

    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CostCenterStatus getStatus() {
        return status;
    }

    public void setStatus(CostCenterStatus status) {
        this.status = status;
    }
}