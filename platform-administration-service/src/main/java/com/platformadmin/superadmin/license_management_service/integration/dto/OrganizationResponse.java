package com.example.platformadmin.superadmin.license_management_service.integration.dto;

import java.util.UUID;

public record OrganizationResponse(

        UUID id,
        String name,
        String status

) {
}
