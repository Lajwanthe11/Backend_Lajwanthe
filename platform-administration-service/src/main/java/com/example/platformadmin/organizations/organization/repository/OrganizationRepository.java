package com.example.platformadmin.organizations.organization.repository;

import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import com.example.platformadmin.organizations.organization.enums.OrganizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    Optional<OrganizationEntity> findByOrganizationCode(String organizationCode);

    boolean existsByOrganizationCodeIgnoreCase(String organizationCode);

    boolean existsByOrganizationCodeIgnoreCaseAndIdNot(String organizationCode, UUID id);

    List<OrganizationEntity> findByOrganizationType(OrganizationType organizationType);

    @Query("""
            SELECT o FROM OrganizationEntity o
                        WHERE LOWER(o.organizationCode) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(o.organizationName) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(o.industry) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(o.city) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(o.country) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(CAST(o.organizationType AS string)) LIKE LOWER(CONCAT('%', :query, '%'))
                        ORDER BY o.organizationName ASC
            """)
    List<OrganizationEntity> searchOrganizations(@Param("query") String query);
}