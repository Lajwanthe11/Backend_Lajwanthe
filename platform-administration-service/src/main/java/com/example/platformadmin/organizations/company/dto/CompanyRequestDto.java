package com.example.platformadmin.organizations.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a Company.
 * Uses Java records for immutability.
 */
public record CompanyRequestDto(

        @NotBlank(message = "Company code is required")
        @Size(max = 20, message = "Company code must not exceed 20 characters")
        String companyCode,

        @NotBlank(message = "Company name is required")
        @Size(max = 100, message = "Company name must not exceed 100 characters")
        String companyName,

        @Email(message = "Email must be valid")
        String email,

        String industry,

        String country,

        String status
) {
}
