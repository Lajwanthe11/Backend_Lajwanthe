package com.example.platformadmin.organizations.branches.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BranchRequestDTO {

    @NotBlank(message = "Branch code is required")
    @Size(max = 50, message = "Branch code must not exceed 50 characters")
    private String branchCode;

    @NotBlank(message = "Branch name is required")
    @Size(max = 100, message = "Branch name must not exceed 100 characters")
    private String branchName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;
}