package com.example.auth.repository;

import com.example.auth.entity.MfaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MfaPolicyRepository
        extends JpaRepository<MfaPolicy, Long> {

    Optional<MfaPolicy> findByOrganizationId(
            String organizationId
    );
}


