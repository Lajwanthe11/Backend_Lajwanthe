package com.example.microservice.auth.controller;

import com.example.microservice.auth.entity.TrustedDevice;
import com.example.microservice.auth.service.MfaAuditLogService;
import com.example.microservice.auth.service.TrustedDeviceService;
import com.example.microservice.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/mfa/trusted-device")
public class TrustedDeviceController {

    private final TrustedDeviceService trustedDeviceService;
    private final MfaAuditLogService auditLogService;

    public TrustedDeviceController(
            TrustedDeviceService trustedDeviceService,
            MfaAuditLogService auditLogService) {

        this.trustedDeviceService =
                trustedDeviceService;

        this.auditLogService =
                auditLogService;
    }

    // ---------------------------------------------------------------
    // Register Trusted Device
    // ---------------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestParam String username) {

        String deviceToken =
                trustedDeviceService
                        .registerTrustedDevice(username);

        auditLogService.logEvent(
                username,
                "default",
                "TRUSTED_DEVICE_REGISTERED",
                true,
                "Trusted device registered successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Trusted device registered successfully",
                        deviceToken
                )
        );
    }

    // ---------------------------------------------------------------
    // Trusted Device Status
    // ---------------------------------------------------------------

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> status(
            @RequestParam String username,
            @RequestParam String deviceToken) {

        boolean trusted =
                trustedDeviceService
                        .isTrustedDevice(
                                username,
                                deviceToken
                        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Trusted device status fetched successfully",
                        trusted
                )
        );
    }

    // ---------------------------------------------------------------
    // Revoke Trusted Device
    // ---------------------------------------------------------------

    @DeleteMapping("/revoke")
    public ResponseEntity<ApiResponse<String>> revoke(
            @RequestParam String username,
            @RequestParam String deviceToken) {

        trustedDeviceService.revokeTrustedDevice(
                username,
                deviceToken
        );

        auditLogService.logEvent(
                username,
                "default",
                "TRUSTED_DEVICE_REVOKED",
                true,
                "Trusted device revoked successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Trusted device revoked successfully",
                        "TRUSTED_DEVICE_REVOKED"
                )
        );
    }

    // ---------------------------------------------------------------
    // Get User Trusted Devices
    // ---------------------------------------------------------------

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<TrustedDevice>>> list(
            @RequestParam String username) {

        List<TrustedDevice> devices =
                trustedDeviceService
                        .getTrustedDevices(username);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Trusted devices fetched successfully",
                        devices
                )
        );
    }

    // ---------------------------------------------------------------
    // Trusted Device Count
    // ---------------------------------------------------------------

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> count() {

        long count =
                trustedDeviceService
                        .countTrustedDevices();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Trusted device count fetched successfully",
                        count
                )
        );
    }
}


