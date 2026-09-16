package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
public class PlatformSummaryResponse {
    private Long totalTenants;
    private Long activeTenants;
    private Long totalOrganizations;
    private Long activeUsers;
    private Long onlineUsers;
    private String platformStatus;
}