package com.example.platformadmin.organizations.businessunit.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class BusinessUnitResponseDto {

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

    public BusinessUnitResponseDto() {
    }

    public BusinessUnitResponseDto(Long id, String unitName, String unitCode, String description,
                                   UUID organizationId, String status, LocalDateTime createdAt,
                                   String createdBy, LocalDateTime updatedAt, String updatedBy) {
        this.id = id;
        this.unitName = unitName;
        this.unitCode = unitCode;
        this.description = description;
        this.organizationId = organizationId;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
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

        public BusinessUnitResponseDto build() {
            return new BusinessUnitResponseDto(id, unitName, unitCode, description, organizationId,
                    status, createdAt, createdBy, updatedAt, updatedBy);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
