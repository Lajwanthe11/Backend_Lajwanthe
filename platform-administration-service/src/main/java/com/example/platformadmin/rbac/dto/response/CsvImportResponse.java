package com.example.platformadmin.rbac.dto.response;

import com.example.platformadmin.rbac.dto.response.CsvImportError;

import java.util.List;

public record CsvImportResponse(
        int totalRows,
        int importedCount,
        int skippedCount,
        int failedCount,
        List<CsvImportError> errors
) {
}
