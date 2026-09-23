package com.example.rbac.dto.response;

import java.util.List;

public record BulkOperationResponse(
        int requestedCount,
        int successCount,
        int skippedCount,
        int failedCount,
        List<BulkOperationItemResult> results
) {
}
