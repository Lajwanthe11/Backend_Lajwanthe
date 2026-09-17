package com.example.microservice.auth.controller;

import com.example.microservice.auth.service.MfaStatisticsService;
import com.example.microservice.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/mfa/statistics")
public class MfaStatisticsController {

    private final MfaStatisticsService mfaStatisticsService;

    public MfaStatisticsController(
            MfaStatisticsService mfaStatisticsService) {

        this.mfaStatisticsService =
                mfaStatisticsService;
    }

    // ---------------------------------------------------------------
    // MFA Statistics
    // ---------------------------------------------------------------

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics(
            @PathVariable String organizationId) {

        long usersEnrolled =
                mfaStatisticsService
                        .getUsersEnrolled(organizationId);

        long pendingEnrolment =
                mfaStatisticsService
                        .getPendingEnrolment(organizationId);

        long trustedDevices =
                mfaStatisticsService
                        .getTrustedDevices();

        Map<String, Long> statistics =
                new LinkedHashMap<>();

        statistics.put(
                "usersEnrolled",
                usersEnrolled
        );

        statistics.put(
                "pendingEnrolment",
                pendingEnrolment
        );

        statistics.put(
                "trustedDevices",
                trustedDevices
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA statistics fetched successfully",
                        statistics
                )
        );
    }
}


