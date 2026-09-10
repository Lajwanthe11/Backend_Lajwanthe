package com.example.platformadmin.organizations.user.dto;

public class BulkUploadResponseDto {

    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;

    public BulkUploadResponseDto() {
    }

    public BulkUploadResponseDto(
            int totalRecords,
            int successfulRecords,
            int failedRecords) {
        this.totalRecords = totalRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
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
}