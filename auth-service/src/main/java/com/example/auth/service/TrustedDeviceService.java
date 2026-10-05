package com.example.auth.service;

import com.example.auth.entity.TrustedDevice;
import com.example.auth.repository.TrustedDeviceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class TrustedDeviceService {

    private final TrustedDeviceRepository trustedDeviceRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${mfa.trusted-device-duration-days:30}")
    private int trustedDeviceDurationDays;

    public TrustedDeviceService(
            TrustedDeviceRepository trustedDeviceRepository) {

        this.trustedDeviceRepository =
                trustedDeviceRepository;
    }

    // ---------------------------------------------------------------
    // Register Trusted Device
    // ---------------------------------------------------------------

    public String registerTrustedDevice(
            String username) {

        String deviceToken =
                generateDeviceToken();

        TrustedDevice trustedDevice =
                new TrustedDevice();

        trustedDevice.setUsername(username);

        trustedDevice.setDeviceToken(
                deviceToken
        );

        trustedDevice.setRegisteredAt(
                LocalDateTime.now()
        );

        trustedDevice.setExpiresAt(
                LocalDateTime.now()
                        .plusDays(
                                trustedDeviceDurationDays
                        )
        );

        trustedDevice.setActive(true);

        trustedDeviceRepository.save(
                trustedDevice
        );

        return deviceToken;
    }

    // ---------------------------------------------------------------
    // Check Trusted Device
    // ---------------------------------------------------------------

    public boolean isTrustedDevice(
            String username,
            String deviceToken) {

        Optional<TrustedDevice> device =
                trustedDeviceRepository
                        .findByUsernameAndDeviceToken(
                                username,
                                deviceToken
                        );

        if (device.isEmpty()) {
            return false;
        }

        TrustedDevice trustedDevice =
                device.get();

        if (!trustedDevice.isActive()) {
            return false;
        }

        if (LocalDateTime.now()
                .isAfter(
                        trustedDevice.getExpiresAt()
                )) {

            trustedDevice.setActive(false);

            trustedDeviceRepository.save(
                    trustedDevice
            );

            return false;
        }

        return true;
    }

    // ---------------------------------------------------------------
    // Revoke Trusted Device
    // ---------------------------------------------------------------

    public void revokeTrustedDevice(
            String username,
            String deviceToken) {

        Optional<TrustedDevice> device =
                trustedDeviceRepository
                        .findByUsernameAndDeviceToken(
                                username,
                                deviceToken
                        );

        if (device.isPresent()) {

            TrustedDevice trustedDevice =
                    device.get();

            trustedDevice.setActive(false);

            trustedDeviceRepository.save(
                    trustedDevice
            );
        }
    }

    // ---------------------------------------------------------------
    // Get User Trusted Devices
    // ---------------------------------------------------------------

    public List<TrustedDevice> getTrustedDevices(
            String username) {

        return trustedDeviceRepository
                .findByUsernameAndActiveTrue(
                        username
                );
    }

    // ---------------------------------------------------------------
    // Count Trusted Devices
    // ---------------------------------------------------------------

    public long countTrustedDevices() {

        return trustedDeviceRepository
                .countByActiveTrue();
    }

    // ---------------------------------------------------------------
    // Generate Device Token
    // ---------------------------------------------------------------

    private String generateDeviceToken() {

        byte[] tokenBytes =
                new byte[32];

        secureRandom.nextBytes(
                tokenBytes
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        tokenBytes
                );
    }
}
