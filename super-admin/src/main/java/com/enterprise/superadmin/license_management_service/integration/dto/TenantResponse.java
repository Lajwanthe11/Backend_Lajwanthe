package com.enterprise.superadmin.license_management_service.integration.dto;

import java.util.UUID;

public record TenantResponse(

        UUID id,
        String name,
        UUID organizationId,
        String status

) {
}
