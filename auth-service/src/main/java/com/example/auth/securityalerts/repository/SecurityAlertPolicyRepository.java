package com.example.auth.securityalerts.repository;

import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SecurityAlertPolicyRepository extends JpaRepository<SecurityAlertPolicy, Long> {

    Optional<SecurityAlertPolicy> findByTenantIdAndEventType(String tenantId, EventType eventType);

    Optional<SecurityAlertPolicy> findByTenantIdIsNullAndEventType(EventType eventType);

    boolean existsByTenantIdIsNullAndEventType(EventType eventType);

    /** Platform defaults plus the given tenant's own policies. */
    @Query("""
            select p from SecurityAlertPolicy p
            where p.tenantId is null or p.tenantId = :tenantId
            order by p.eventType, p.tenantId nulls first
            """)
    List<SecurityAlertPolicy> findGlobalAndTenant(@Param("tenantId") String tenantId);

    @Query("select p from SecurityAlertPolicy p order by p.eventType, p.tenantId nulls first")
    List<SecurityAlertPolicy> findAllOrdered();
}
