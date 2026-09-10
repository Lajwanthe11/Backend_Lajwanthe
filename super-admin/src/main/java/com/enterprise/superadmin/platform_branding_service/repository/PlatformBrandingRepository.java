package com.enterprise.superadmin.platform_branding_service.repository;

import com.enterprise.superadmin.platform_branding_service.entity.PlatformBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformBrandingRepository extends JpaRepository<PlatformBranding, UUID> {

    /**
     * Finds a PlatformBranding entity by its ID, ensuring it is not marked as deleted.
     *
     * @param id the UUID of the PlatformBranding entity
     * @return an Optional containing the PlatformBranding entity if found and not deleted, otherwise an empty Optional
     */
    Optional<PlatformBranding> findByIdAndIsDeletedFalse(UUID id);
    /**
     * Finds the first PlatformBranding entity that is not marked as deleted.
     *
     * @return an Optional containing the first PlatformBranding entity if found and not deleted, otherwise an empty Optional
     */
    Optional<PlatformBranding> findFirstByIsDeletedFalse();
    /**
     * Checks if any PlatformBranding entity exists that is not marked as deleted.
     *
     * @return true if at least one PlatformBranding entity exists and is not deleted, otherwise false
     */
    boolean existsByIsDeletedFalse();
}