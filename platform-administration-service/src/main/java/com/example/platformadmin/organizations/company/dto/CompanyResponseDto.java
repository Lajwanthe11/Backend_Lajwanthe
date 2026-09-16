package com.example.platformadmin.organizations.company.dto;

import java.time.LocalDateTime;

/**
 * Response DTO returned for Company API responses.
 */
public record CompanyResponseDto(
        Long id,
        String companyCode,
        String companyName,
        String email,
        String industry,
        String country,
        String status,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
