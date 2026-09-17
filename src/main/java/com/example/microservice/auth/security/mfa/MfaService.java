package com.example.microservice.auth.security.mfa;

import com.example.microservice.auth.security.user.CustomUserDetailsService;
import com.example.microservice.auth.security.user.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaService {

    @Value("${mfa.otp-validity-minutes:5}")
    private int otpValidityMinutes;

    @Value("${mfa.max-verification-attempts:5}")
    private int maxVerificationAttempts;

    @Value("${mfa.trusted-device-duration-days:30}")
    private int trustedDeviceDurationDays;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, OtpData> otpStore =
            new ConcurrentHashMap<>();

    private final Map<String, List<String>> backupCodeStore =
            new ConcurrentHashMap<>();

    /*
     * Trusted device token -> expiry information
     *
     * Key:
     *     username:deviceToken
     *
     * Value:
     *     expiry time
     */
    private final Map<String, Long> trustedDeviceStore =
            new ConcurrentHashMap<>();

    private final CustomUserDetailsService customUserDetailsService;

    private final JavaMailSender mailSender;

    public MfaService(
            CustomUserDetailsService customUserDetailsService,
            JavaMailSender mailSender) {

        this.customUserDetailsService =
                customUserDetailsService;

        this.mailSender = mailSender;
    }

    // ---------------------------------------------------------------
    // Generate OTP
    // ---------------------------------------------------------------

    public String generateOtp(String username) {

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        long expiryTime =
                System.currentTimeMillis()
                        + (otpValidityMinutes * 60L * 1000L);

        otpStore.put(
                username,
                new OtpData(
                        otp,
                        expiryTime,
                        0
                )
        );

        return otp;
    }

    // ---------------------------------------------------------------
    // Send OTP Email
    // ---------------------------------------------------------------

    public void sendOtpEmail(String username) {

        UserPrincipal user =
                (UserPrincipal) customUserDetailsService
                        .loadUserByUsername(username);

        String email = user.getEmail();

        String otp = generateOtp(username);

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailUsername);
        message.setTo(email);
        message.setSubject("MFA Verification Code");

        message.setText(
                "Hello " + username + ",\n\n"
                        + "Your MFA verification code is: "
                        + otp + "\n\n"
                        + "This OTP is valid for "
                        + otpValidityMinutes
                        + " minutes.\n\n"
                        + "Please do not share this code with anyone.\n\n"
                        + "Regards,\n"
                        + "Auth Service"
        );

        mailSender.send(message);
    }
    // ---------------------------------------------------------------
    // Verify OTP
    // ---------------------------------------------------------------

    public boolean verifyOtp(
            String username,
            String otp) {

        OtpData data =
                otpStore.get(username);

        if (data == null) {
            return false;
        }

        if (System.currentTimeMillis()
                > data.expiryTime()) {

            otpStore.remove(username);

            return false;
        }

        if (data.attempts()
                >= maxVerificationAttempts) {

            otpStore.remove(username);

            return false;
        }

        if (!data.otp().equals(otp)) {

            int updatedAttempts =
                    data.attempts() + 1;

            if (updatedAttempts
                    >= maxVerificationAttempts) {

                otpStore.remove(username);

            } else {

                otpStore.put(
                        username,
                        new OtpData(
                                data.otp(),
                                data.expiryTime(),
                                updatedAttempts
                        )
                );
            }

            return false;
        }

        otpStore.remove(username);

        return true;
    }

    // ---------------------------------------------------------------
    // Enable MFA
    // ---------------------------------------------------------------

    public void enableMfa(
            String username,
            String tenantId) {

        customUserDetailsService.updateMfaStatus(
                username,
                tenantId,
                true
        );
    }

    // ---------------------------------------------------------------
    // Disable MFA
    // ---------------------------------------------------------------

    public void disableMfa(
            String username,
            String tenantId) {

        customUserDetailsService.updateMfaStatus(
                username,
                tenantId,
                false
        );
    }

    // ---------------------------------------------------------------
    // Check MFA Status
    // ---------------------------------------------------------------

    public boolean isMfaEnabled(
            String username,
            String tenantId) {

        return customUserDetailsService
                .isMfaEnabled(
                        username,
                        tenantId
                );
    }

    // ---------------------------------------------------------------
    // Generate Backup Codes
    // ---------------------------------------------------------------

    public List<String> generateBackupCodes(
            String username) {

        List<String> backupCodes =
                new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            String code =
                    String.format(
                            "%08d",
                            secureRandom.nextInt(
                                    100_000_000
                            )
                    );

            backupCodes.add(code);
        }

        backupCodeStore.put(
                username,
                new ArrayList<>(backupCodes)
        );

        return backupCodes;
    }

    // ---------------------------------------------------------------
    // Regenerate Backup Codes
    // ---------------------------------------------------------------

    public List<String> regenerateBackupCodes(
            String username) {

        backupCodeStore.remove(username);

        return generateBackupCodes(username);
    }

    // ---------------------------------------------------------------
    // Verify Backup Code
    // ---------------------------------------------------------------

    public boolean verifyBackupCode(
            String username,
            String backupCode) {

        List<String> backupCodes =
                backupCodeStore.get(username);

        if (backupCodes == null) {
            return false;
        }

        if (!backupCodes.contains(backupCode)) {
            return false;
        }

        backupCodes.remove(backupCode);

        if (backupCodes.isEmpty()) {
            backupCodeStore.remove(username);
        }

        return true;
    }

    // ---------------------------------------------------------------
    // Register Trusted Device
    // ---------------------------------------------------------------

    public String registerTrustedDevice(
            String username) {

        byte[] tokenBytes = new byte[32];

        secureRandom.nextBytes(tokenBytes);

        String deviceToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(tokenBytes);

        long expiryTime =
                System.currentTimeMillis()
                        + (trustedDeviceDurationDays
                        * 24L
                        * 60L
                        * 60L
                        * 1000L);

        trustedDeviceStore.put(
                buildTrustedDeviceKey(
                        username,
                        deviceToken
                ),
                expiryTime
        );

        return deviceToken;
    }

    // ---------------------------------------------------------------
    // Check Trusted Device
    // ---------------------------------------------------------------

    public boolean isTrustedDevice(
            String username,
            String deviceToken) {

        if (deviceToken == null ||
                deviceToken.isBlank()) {

            return false;
        }

        String key =
                buildTrustedDeviceKey(
                        username,
                        deviceToken
                );

        Long expiryTime =
                trustedDeviceStore.get(key);

        if (expiryTime == null) {
            return false;
        }

        if (System.currentTimeMillis()
                > expiryTime) {

            trustedDeviceStore.remove(key);

            return false;
        }

        return true;
    }

    // ---------------------------------------------------------------
    // Revoke Trusted Device
    // ---------------------------------------------------------------

    public boolean revokeTrustedDevice(
            String username,
            String deviceToken) {

        if (deviceToken == null ||
                deviceToken.isBlank()) {

            return false;
        }

        String key =
                buildTrustedDeviceKey(
                        username,
                        deviceToken
                );

        return trustedDeviceStore.remove(key) != null;
    }

    // ---------------------------------------------------------------
    // Build Trusted Device Key
    // ---------------------------------------------------------------

    private String buildTrustedDeviceKey(
            String username,
            String deviceToken) {

        return username + ":" + deviceToken;
    }

    // ---------------------------------------------------------------
    // OTP Data
    // ---------------------------------------------------------------

    private record OtpData(
            String otp,
            long expiryTime,
            int attempts
    ) {
    }
}


