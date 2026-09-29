package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAlertResponse {
    private String id;
    private String title;
    private String message;
    private String severity;       // "INFO", "WARNING", "CRITICAL"
    private String module;         // "LICENSES", "SYSTEM_HEALTH", "SECURITY"
    private LocalDateTime timestamp;
    private String actionUrl;
}
