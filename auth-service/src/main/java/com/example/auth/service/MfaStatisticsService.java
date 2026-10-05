package com.example.auth.service;

import com.example.auth.security.user.CustomUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class MfaStatisticsService {

    private final CustomUserDetailsService customUserDetailsService;
    private final TrustedDeviceService trustedDeviceService;

    public MfaStatisticsService(
            CustomUserDetailsService customUserDetailsService,
            TrustedDeviceService trustedDeviceService) {

        this.customUserDetailsService =
                customUserDetailsService;

        this.trustedDeviceService =
                trustedDeviceService;
    }

    // ---------------------------------------------------------------
    // Users Enrolled
    // ---------------------------------------------------------------

    public long getUsersEnrolled(String organizationId) {

        return customUserDetailsService
                .countMfaEnrolledUsers(organizationId);
    }

    // ---------------------------------------------------------------
    // Pending Enrolment
    // ---------------------------------------------------------------

    public long getPendingEnrolment(String organizationId) {

        return customUserDetailsService
                .countMfaPendingUsers(organizationId);
    }

    // ---------------------------------------------------------------
    // Trusted Devices
    // ---------------------------------------------------------------

    public long getTrustedDevices() {

        return trustedDeviceService
                .countTrustedDevices();
    }
}
