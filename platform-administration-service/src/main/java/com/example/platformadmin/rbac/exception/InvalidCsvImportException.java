package com.example.platformadmin.rbac.exception;

public class InvalidCsvImportException extends RuntimeException {
    public InvalidCsvImportException(String message) {
        super(message);
    }
}
