package com.example.platformadmin.organizations.businessunit.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BusinessUnit entity extending the platform's BaseEntity.
 * Inherits: id (Long), tenantId, createdAt, updatedAt, createdBy, updatedBy, version.
 */
@Getter
@Setter
@NoArgsConstructor
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

    @Builder
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
}
