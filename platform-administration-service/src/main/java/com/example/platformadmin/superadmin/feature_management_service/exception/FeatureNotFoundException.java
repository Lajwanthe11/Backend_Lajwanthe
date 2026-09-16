package com.example.platformadmin.superadmin.feature_management_service.exception;

public class FeatureNotFoundException extends RuntimeException {

    public FeatureNotFoundException(String message) {
        super(message);
    }
}