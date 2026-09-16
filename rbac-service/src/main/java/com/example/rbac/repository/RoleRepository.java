package com.example.rbac.repository;

import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    boolean existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(String roleName, UUID tenantId);

    boolean existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(String roleCode, UUID tenantId);

    Optional<Role> findByIdAndTenantIdAndIsDeletedFalse(UUID id,UUID tenantId);

    List<Role> findByTenantIdAndIsDeletedFalse(UUID tenantId);

    Page<Role> findByTenantIdAndIsDeletedFalse(UUID tenantId, Pageable pageable);

    @Query("""
        SELECT r FROM Role r
            WHERE r.tenantId = :tenantId
            AND r.isDeleted = false
            AND (:query IS NULL
                OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(r.roleCode) LIKE LOWER(CONCAT('%', :query, '%')))
            AND (:roleType IS NULL OR r.roleType = :roleType)
            AND (:status IS NULL OR r.status = :status)""")
    List<Role> searchRoles( @Param("tenantId") UUID tenantId, @Param("query") String query, @Param("roleType") RoleType roleType, @Param("status") String status);

    long countByTenantIdAndIsDeletedFalse( UUID tenantId);

    long countByTenantIdAndRoleTypeAndIsDeletedFalse( UUID tenantId, RoleType roleType);

    Optional<Role> findByIdAndTenantId(UUID id,UUID tenantId);

    List<Role> findAllByTenantIdAndRoleType( UUID tenantId, RoleType roleType);

    boolean existsByTenantIdAndRoleCode(UUID tenantId,String roleCode);

    List<Role> findAllByTenantId( UUID tenantId);
}