package com.example.auth.controller;

import com.example.auth.entity.MfaAuditLog;
import com.example.auth.service.MfaAuditLogService;
import com.example.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/mfa/audit")
public class MfaAuditLogController {

    private final MfaAuditLogService auditLogService;

    public MfaAuditLogController(
            MfaAuditLogService auditLogService) {

        this.auditLogService =
                auditLogService;
    }

    // ---------------------------------------------------------------
    // Get User Audit Logs
    // ---------------------------------------------------------------

    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<List<MfaAuditLog>>> getUserAuditLogs(
            @PathVariable String username) {

        List<MfaAuditLog> logs =
                auditLogService.getUserAuditLogs(
                        username
                );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "User MFA audit logs fetched successfully",
                        logs
                )
        );
    }

    // ---------------------------------------------------------------
    // Get Organization Audit Logs
    // ---------------------------------------------------------------

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<ApiResponse<List<MfaAuditLog>>> getOrganizationAuditLogs(
            @PathVariable String organizationId) {

        List<MfaAuditLog> logs =
                auditLogService.getOrganizationAuditLogs(
                        organizationId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Organization MFA audit logs fetched successfully",
                        logs
                )
        );
    }
}


