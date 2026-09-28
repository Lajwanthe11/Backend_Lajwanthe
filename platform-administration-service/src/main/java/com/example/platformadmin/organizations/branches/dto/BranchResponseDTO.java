package com.example.platformadmin.organizations.branches.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BranchResponseDTO {

    private Long id;
    private String branchCode;
    private String branchName;
    private String description;
    private String status;

    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
}