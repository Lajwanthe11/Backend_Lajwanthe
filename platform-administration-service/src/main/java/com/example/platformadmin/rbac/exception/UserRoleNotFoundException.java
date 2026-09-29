package com.example.platformadmin.rbac.exception;

public class UserRoleNotFoundException extends RuntimeException {

    public UserRoleNotFoundException(String message) {
        super(message);
    }
}
