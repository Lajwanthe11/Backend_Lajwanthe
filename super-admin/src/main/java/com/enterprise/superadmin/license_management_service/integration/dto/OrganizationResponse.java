package com.enterprise.superadmin.license_management_service.integration.dto;

import java.util.UUID;

public record OrganizationResponse(

        UUID id,
        String name,
        String status

) {
}
