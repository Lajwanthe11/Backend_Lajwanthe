package com.example.rbac.dto.response;

import com.example.rbac.dto.CsvImportError;

import java.util.List;

public record CsvImportResponse(
        int totalRows,
        int importedCount,
        int skippedCount,
        int failedCount,
        List<CsvImportError> errors
) {
}
