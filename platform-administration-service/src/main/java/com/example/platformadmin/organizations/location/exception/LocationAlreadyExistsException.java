package com.example.platformadmin.organizations.location.exception;

public class LocationAlreadyExistsException extends RuntimeException {

    public LocationAlreadyExistsException(String message) {
        super(message);
    }
}