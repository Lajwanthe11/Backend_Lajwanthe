package com.example.rbac.dto;

import java.util.UUID;

public record BulkOperationItemResult(
        UUID userId,
        String status,
        String message
) {
}
