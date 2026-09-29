package com.example.platformadmin.rbac.dto.response;

import java.util.UUID;

public record BulkOperationItemResult(
        UUID userId,
        String status,
        String message
) {
}
