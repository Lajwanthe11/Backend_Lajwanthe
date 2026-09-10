package com.example.platformadmin.organizations.user.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportSummaryDto {

    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private List<ImportErrorDto> errors = new ArrayList<>();

    public ImportSummaryDto() {
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessfulRecords() {
        return successfulRecords;
    }

    public void setSuccessfulRecords(int successfulRecords) {
        this.successfulRecords = successfulRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public List<ImportErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportErrorDto> errors) {
        this.errors = errors;
    }
}