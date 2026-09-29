package com.example.auth.repository;

import com.example.auth.entity.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrustedDeviceRepository
        extends JpaRepository<TrustedDevice, Long> {

    Optional<TrustedDevice> findByUsernameAndDeviceToken(
            String username,
            String deviceToken
    );

    List<TrustedDevice> findByUsernameAndActiveTrue(
            String username
    );

    long countByActiveTrue();

    long countByUsernameAndActiveTrue(
            String username
    );
}


