package com.example.platformadmin.organizations.businessunit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitRequestDto {

    @NotBlank(message = "Unit name is required")
    private String unitName;

    @NotBlank(message = "Unit code is required")
    private String unitCode;

    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private String status = "ACTIVE";
}
