package com.example.platformadmin.organizations.businessunit.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BusinessUnit entity extending the platform's BaseEntity.
 * Inherits: id (Long), tenantId, createdAt, updatedAt, createdBy, updatedBy, version.
 */
@Entity
@Table(name = "business_units", schema = "public")
public class BusinessUnit extends BaseEntity {

    @Column(name = "unit_name", nullable = false)
    private String unitName;

    @Column(name = "unit_code", nullable = false, unique = true)
    private String unitCode;

    @Column(name = "description")
    private String description;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    public BusinessUnit() {
        super();
    }

    public BusinessUnit(Long id, String unitName, String unitCode, String description,
                        UUID organizationId, String status, LocalDateTime createdAt,
                        String createdBy, LocalDateTime updatedAt, String updatedBy,
                        Boolean isDeleted, LocalDate deletedAt, String deletedBy,
                        String tenantId, Long version) {
        super();
        this.setId(id);
        this.setTenantId(tenantId);
        this.setCreatedAt(createdAt);
        this.setCreatedBy(createdBy);
        this.setUpdatedAt(updatedAt);
        this.setUpdatedBy(updatedBy);
        this.setVersion(version);
        this.unitName = unitName;
        this.unitCode = unitCode;
        this.description = description;
        this.organizationId = organizationId;
        this.status = status;
        this.isDeleted = isDeleted != null ? isDeleted : false;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String unitName;
        private String unitCode;
        private String description;
        private UUID organizationId;
        private String status;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;
        private Boolean isDeleted = false;
        private LocalDate deletedAt;
        private String deletedBy;
        private String tenantId;
        private Long version;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder unitName(String unitName) { this.unitName = unitName; return this; }
        public Builder unitCode(String unitCode) { this.unitCode = unitCode; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder organizationId(UUID organizationId) { this.organizationId = organizationId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }
        public Builder isDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; return this; }
        public Builder deletedAt(LocalDate deletedAt) { this.deletedAt = deletedAt; return this; }
        public Builder deletedBy(String deletedBy) { this.deletedBy = deletedBy; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder version(Long version) { this.version = version; return this; }

        public BusinessUnit build() {
            return new BusinessUnit(id, unitName, unitCode, description, organizationId, status,
                    createdAt, createdBy, updatedAt, updatedBy, isDeleted, deletedAt, deletedBy,
                    tenantId, version);
        }
    }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public LocalDate getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDate deletedAt) { this.deletedAt = deletedAt; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
}
