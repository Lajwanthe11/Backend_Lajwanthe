package com.example.platformadmin.superadmin.platform_branding_service.dto.response;

/**
 * Represents validation failure for a specific request field.
 */
public class FieldErrorResponse {

    private String field;

    private String message;

    public FieldErrorResponse() {
    }

    public FieldErrorResponse(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}