package com.example.rbac.dto;

public record CsvImportError(
        long rowNumber,
        String employeeId,
        String roleCode,
        String message
) {
}
