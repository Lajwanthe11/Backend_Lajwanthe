package com.example.platformadmin.organizations.branches.exception;

public class BranchAlreadyExistsException extends RuntimeException {

    public BranchAlreadyExistsException(String message) {
        super(message);
    }
}