package com.example.platformadmin.organizations.businessunit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BusinessUnitRequestDto {

    @NotBlank(message = "Unit name is required")
    private String unitName;

    @NotBlank(message = "Unit code is required")
    private String unitCode;

    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private String status = "ACTIVE";

    public BusinessUnitRequestDto() {
    }

    public BusinessUnitRequestDto(String unitName, String unitCode, String description,
                                  UUID organizationId, String status) {
        this.unitName = unitName;
        this.unitCode = unitCode;
        this.description = description;
        this.organizationId = organizationId;
        this.status = status != null ? status : "ACTIVE";
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
}
