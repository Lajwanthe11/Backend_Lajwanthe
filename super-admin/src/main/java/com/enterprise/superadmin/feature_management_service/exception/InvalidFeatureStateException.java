package com.enterprise.superadmin.feature_management_service.exception;

public class InvalidFeatureStateException extends RuntimeException{
    public InvalidFeatureStateException(String message){
        super(message);
    }
}
