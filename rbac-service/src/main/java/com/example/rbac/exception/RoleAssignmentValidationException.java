package com.example.rbac.exception;

public class RoleAssignmentValidationException extends RuntimeException {
    public RoleAssignmentValidationException(String message) {
        super(message);
    }
}
