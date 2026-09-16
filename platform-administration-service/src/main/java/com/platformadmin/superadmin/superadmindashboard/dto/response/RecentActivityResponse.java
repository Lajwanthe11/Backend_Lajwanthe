package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecentActivityResponse {
    private Long auditId;
    private Long userId;
    private String username;
    private String role;
    private String activity;
    private String module;
    private String status;
    private LocalDateTime dateTime;
}
