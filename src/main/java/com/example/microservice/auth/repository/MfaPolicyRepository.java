package com.example.microservice.auth.repository;

import com.example.microservice.auth.entity.MfaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MfaPolicyRepository
        extends JpaRepository<MfaPolicy, Long> {

    Optional<MfaPolicy> findByOrganizationId(
            String organizationId
    );
}


