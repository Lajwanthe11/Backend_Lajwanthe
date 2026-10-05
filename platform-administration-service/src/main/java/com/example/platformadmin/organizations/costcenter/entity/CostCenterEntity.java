package com.example.platformadmin.organizations.costcenter.entity;

import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cost_centers")
public class CostCenterEntity {

    // =========================================================
    // ID
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // COST CENTER DETAILS
    // =========================================================

    @Column(
            name = "cost_center_code",
            nullable = false,
            length = 255
    )
    private String costCenterCode;

    @Column(
            name = "cost_center_name",
            nullable = false,
            length = 255
    )
    private String costCenterName;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    // =========================================================
    // ORGANIZATION
    // =========================================================

    @Column(
            name = "organization_id",
            nullable = false
    )
    private UUID organizationId;

    // =========================================================
    // COMPANY
    // =========================================================

    @Column(name = "company_id")
    private Long companyId;

    // =========================================================
    // DEPARTMENT
    // =========================================================

    @Column(name = "department_id")
    private Long departmentId;

    // =========================================================
    // BUDGET
    // =========================================================

    @Column(
            name = "allocated_budget",
            precision = 15,
            scale = 2
    )
    private BigDecimal allocatedBudget;

    // =========================================================
    // CURRENCY
    // =========================================================

    @Column(
            name = "currency",
            length = 10
    )
    private String currency = "USD";

    // =========================================================
    // STATUS
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private CostCenterStatus status = CostCenterStatus.ACTIVE;

    // =========================================================
    // AUDIT FIELDS
    // =========================================================

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "version")
    private Long version;

    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public void setAllocatedBudget(BigDecimal allocatedBudget) {
        this.allocatedBudget = allocatedBudget;
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}