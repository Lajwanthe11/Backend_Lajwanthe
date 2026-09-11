package com.example.rbac.repository;

import com.example.rbac.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserIdAndTenantIdAndActiveTrue(
            UUID userId,
            UUID tenantId
    );

    Optional<UserRole>
    findByUserIdAndRoleIdAndTenantIdAndActiveTrue(
            UUID userId,
            UUID roleId,
            UUID tenantId
    );

    Optional<UserRole>
    findByUserIdAndTenantIdAndPrimaryTrueAndActiveTrue(
            UUID userId,
            UUID tenantId
    );

    List<UserRole>
    findByUserIdAndTenantIdOrderByAssignedAtDesc(
            UUID userId,
            UUID tenantId
    );

    List<UserRole>
    findByRoleIdAndTenantIdAndActiveTrue(
            UUID roleId,
            UUID tenantId
    );

    @Query("""
        SELECT ur
        FROM UserRole ur
        WHERE ur.userId = :userId
          AND ur.tenantId = :tenantId
          AND ur.active = true
          AND ur.effectiveDate <= :today
          AND (
              ur.expiryDate IS NULL
              OR ur.expiryDate > :today
          )
        ORDER BY ur.primary DESC, ur.assignedAt DESC
    """)
    List<UserRole> findCurrentRoles(
            @Param("userId") UUID userId,
            @Param("tenantId") UUID tenantId,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT ur
        FROM UserRole ur
        WHERE ur.expiryDate <= :today
          AND ur.active = true
    """)
    List<UserRole> findExpiredRoles(
            @Param("today") LocalDate today
    );

    @Modifying
    @Query("""
        UPDATE UserRole ur
        SET ur.active = false,
            ur.primary = false
        WHERE ur.roleId = :roleId
          AND ur.tenantId = :tenantId
          AND ur.active = true
    """)
    int deactivateRolesForRole(
            @Param("roleId") UUID roleId,
            @Param("tenantId") UUID tenantId
    );

    boolean existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
            UUID tenantId, UUID userId, UUID roleId);

    Optional<UserRole> findFirstByTenantIdAndUserIdAndRoleIdAndActiveTrue(
            UUID tenantId, UUID userId, UUID roleId);

    List<UserRole> findAllByTenantId(UUID tenantId);

    @Query("""
            select ur from UserRole ur
            where ur.tenantId = :tenantId
              and ur.active = true
              and ur.effectiveDate <= :today
              and (ur.expiryDate is null or ur.expiryDate >= :today)
            """)
    List<UserRole> findCurrentActiveAssignments(
            @Param("tenantId") UUID tenantId,
            @Param("today") LocalDate today);

    @Query("""
            select count(ur) from UserRole ur
            where ur.tenantId = :tenantId
              and ur.userId = :userId
              and ur.active = true
              and ur.effectiveDate <= :today
              and (ur.expiryDate is null or ur.expiryDate >= :today)
            """)
    long countCurrentActiveAssignmentsForUser(
            @Param("tenantId") UUID tenantId,
            @Param("userId") UUID userId,
            @Param("today") LocalDate today);

    @Query("""
            select ur from UserRole ur
            where ur.tenantId = :tenantId
              and ur.active = true
              and ur.effectiveDate <= :today
              and ur.expiryDate between :today and :endDate
            """)
    List<UserRole> findExpiringAssignments(
            @Param("tenantId") UUID tenantId,
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate);

    @Query("""
            select ur from UserRole ur
            where ur.tenantId = :tenantId
              and ur.active = true
              and ur.effectiveDate <= :today
              and ur.expiryDate = :targetDate
            """)
    List<UserRole> findTenantAssignmentsExpiringOn(
            @Param("tenantId") UUID tenantId,
            @Param("today") LocalDate today,
            @Param("targetDate") LocalDate targetDate);

    @Query("""
            select ur from UserRole ur
            where ur.active = true
              and ur.effectiveDate <= :today
              and ur.expiryDate = :targetDate
            """)
    List<UserRole> findAllAssignmentsExpiringOn(
            @Param("today") LocalDate today,
            @Param("targetDate") LocalDate targetDate);
}