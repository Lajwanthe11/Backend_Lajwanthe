package com.example.rbac.dto;

import java.util.List;

public record CsvImportResponse(
        int totalRows,
        int importedCount,
        int skippedCount,
        int failedCount,
        List<CsvImportError> errors
) {
}
