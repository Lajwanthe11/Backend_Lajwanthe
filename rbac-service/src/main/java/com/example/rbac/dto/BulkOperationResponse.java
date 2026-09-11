package com.example.rbac.dto;

import java.util.List;

public record BulkOperationResponse(
        int requestedCount,
        int successCount,
        int skippedCount,
        int failedCount,
        List<BulkOperationItemResult> results
) {
}
