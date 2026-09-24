package com.example.platformadmin.organizations.organization.dto;

import com.example.platformadmin.organizations.organization.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponseDTO {

    private UUID id;
    private String organizationName;
    private String organizationCode;
    private OrganizationType organizationType;
    private String organizationTypeDisplayName;
    private String industry;
    private String companySize;
    private String country;
    private String state;
    private String city;
    private String timeZone;
    private String logoUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}