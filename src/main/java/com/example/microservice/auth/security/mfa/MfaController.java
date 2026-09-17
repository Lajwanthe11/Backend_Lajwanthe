package com.example.microservice.auth.security.mfa;

import com.example.microservice.auth.dto.AuthResponse;
import com.example.microservice.auth.dto.MfaVerificationRequestDTO;
import com.example.microservice.auth.service.AuthService;
import com.example.microservice.auth.service.MfaAuditLogService;
import com.example.microservice.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/mfa")
public class MfaController {

    private final MfaService mfaService;
    private final AuthService authService;
    private final MfaAuditLogService auditLogService;

    public MfaController(
            MfaService mfaService,
            AuthService authService,
            MfaAuditLogService auditLogService) {

        this.mfaService = mfaService;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    // ---------------------------------------------------------------
    // MFA Verification
    // ---------------------------------------------------------------

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verify(
            @Valid @RequestBody MfaVerificationRequestDTO request) {

        boolean verified =
                mfaService.verifyOtp(
                        request.getUsername(),
                        request.getOtp()
                );

        String organizationId =
                request.getTenantId() != null
                        ? request.getTenantId()
                        : "default";

        if (!verified) {

            auditLogService.logEvent(
                    request.getUsername(),
                    organizationId,
                    "OTP_VERIFICATION_FAILED",
                    false,
                    "Invalid or expired OTP",
                    null
            );

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Invalid or expired OTP"
                    ));
        }

        AuthResponse authResponse =
                authService.completeMfaLogin(
                        request.getUsername(),
                        request.getTenantId()
                );

        auditLogService.logEvent(
                request.getUsername(),
                organizationId,
                "OTP_VERIFICATION_SUCCESS",
                true,
                "MFA OTP verification successful",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA verification successful",
                        authResponse
                )
        );
    }

    // ---------------------------------------------------------------
    // Enable MFA
    // ---------------------------------------------------------------

    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<String>> enableMfa(
            @RequestParam String username,
            @RequestParam(required = false) String tenantId) {

        mfaService.enableMfa(
                username,
                tenantId
        );

        String organizationId =
                tenantId != null ? tenantId : "default";

        auditLogService.logEvent(
                username,
                organizationId,
                "MFA_ENABLED",
                true,
                "MFA enabled successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA enabled successfully",
                        "MFA_ENABLED"
                )
        );
    }

    // ---------------------------------------------------------------
    // Disable MFA
    // ---------------------------------------------------------------

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<String>> disableMfa(
            @RequestParam String username,
            @RequestParam(required = false) String tenantId) {

        mfaService.disableMfa(
                username,
                tenantId
        );

        String organizationId =
                tenantId != null ? tenantId : "default";

        auditLogService.logEvent(
                username,
                organizationId,
                "MFA_DISABLED",
                true,
                "MFA disabled successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA disabled successfully",
                        "MFA_DISABLED"
                )
        );
    }

    // ---------------------------------------------------------------
    // View MFA Status
    // ---------------------------------------------------------------

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> getMfaStatus(
            @RequestParam String username,
            @RequestParam(required = false) String tenantId) {

        boolean enabled =
                mfaService.isMfaEnabled(
                        username,
                        tenantId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA status fetched successfully",
                        enabled
                )
        );
    }
    // ---------------------------------------------------------------
// Test MFA
// ---------------------------------------------------------------

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<String>> testMfa(
            @RequestParam String username) {

        try {
            mfaService.sendOtpEmail(username);

            auditLogService.logEvent(
                    username,
                    "default",
                    "MFA_TEST",
                    true,
                    "Test MFA OTP sent successfully",
                    null
            );

            return ResponseEntity.ok(
                    ApiResponse.ok(
                            "Test MFA OTP sent successfully",
                            "TEST_MFA_SENT"
                    )
            );

        } catch (Exception e) {

            auditLogService.logEvent(
                    username,
                    "default",
                    "MFA_TEST",
                    false,
                    "Test MFA OTP sending failed",
                    null
            );

            throw e;
        }
    }

    // ---------------------------------------------------------------
    // Generate Backup Codes
    // ---------------------------------------------------------------

    @PostMapping("/backup-codes/generate")
    public ResponseEntity<ApiResponse<List<String>>> generateBackupCodes(
            @RequestParam String username) {

        List<String> backupCodes =
                mfaService.generateBackupCodes(
                        username
                );

        auditLogService.logEvent(
                username,
                "default",
                "BACKUP_CODES_GENERATED",
                true,
                "MFA backup codes generated successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Backup codes generated successfully",
                        backupCodes
                )
        );
    }

    // ---------------------------------------------------------------
    // Regenerate Backup Codes
    // ---------------------------------------------------------------

    @PostMapping("/backup-codes/regenerate")
    public ResponseEntity<ApiResponse<List<String>>> regenerateBackupCodes(
            @RequestParam String username) {

        List<String> backupCodes =
                mfaService.regenerateBackupCodes(
                        username
                );

        auditLogService.logEvent(
                username,
                "default",
                "BACKUP_CODES_REGENERATED",
                true,
                "MFA backup codes regenerated successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Backup codes regenerated successfully",
                        backupCodes
                )
        );
    }

    // ---------------------------------------------------------------
    // Verify Backup Code
    // ---------------------------------------------------------------

    @PostMapping("/backup-codes/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyBackupCode(
            @RequestParam String username,
            @RequestParam String backupCode,
            @RequestParam(required = false) String tenantId) {

        boolean verified =
                mfaService.verifyBackupCode(
                        username,
                        backupCode
                );

        String organizationId =
                tenantId != null ? tenantId : "default";

        if (!verified) {

            auditLogService.logEvent(
                    username,
                    organizationId,
                    "BACKUP_CODE_VERIFICATION_FAILED",
                    false,
                    "Invalid or already used backup code",
                    null
            );

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Invalid or already used backup code"
                    ));
        }

        AuthResponse authResponse =
                authService.completeMfaLogin(
                        username,
                        tenantId
                );

        auditLogService.logEvent(
                username,
                organizationId,
                "BACKUP_CODE_VERIFICATION_SUCCESS",
                true,
                "MFA backup code verification successful",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Backup code verification successful",
                        authResponse
                )
        );
    }
}


