package com.example.platformadmin.rbac.dto.response;

public record CsvImportError(
        long rowNumber,
        String employeeId,
        String roleCode,
        String message
) {
}
