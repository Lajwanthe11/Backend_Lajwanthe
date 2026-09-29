package com.example.platformadmin.organizations.organization.dto;

import com.example.platformadmin.organizations.organization.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationRequestDTO {

    @NotBlank(message = "Organization name is required")
    @Size(max = 150, message = "Organization name must not exceed 150 characters")
    private String organizationName;

    @NotBlank(message = "Organization code is required")
    @Size(max = 50, message = "Organization code must not exceed 50 characters")
    private String organizationCode;

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    @NotBlank(message = "Industry is required")
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    @NotBlank(message = "Company size is required")
    @Size(max = 50, message = "Company size must not exceed 50 characters")
    private String companySize;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Time zone is required")
    @Size(max = 50, message = "Time zone must not exceed 50 characters")
    private String timeZone;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    private String status = "ACTIVE";
}