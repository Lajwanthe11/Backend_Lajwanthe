package com.example.rbac.exception;

public class InvalidCsvImportException extends RuntimeException {
    public InvalidCsvImportException(String message) {
        super(message);
    }
}
