package com.example.rbac.repository;

import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, Long> {
    
boolean existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse( String roleName, String tenantId);

boolean existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(String roleCode, String tenantId);

Optional<Role> findByIdAndTenantIdAndIsDeletedFalse(Long id, String tenantId);

List<Role> findByTenantIdAndIsDeletedFalse(String tenantId);

Page<Role> findByTenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);
// Search roles within current tenant
@Query("SELECT r FROM Role r " +
       "WHERE r.tenantId = :tenantId " +
       "AND r.isDeleted = false " +
       "AND (:query IS NULL OR :query = '' " +
       "OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :query, '%')) " +
       "OR LOWER(r.roleCode) LIKE LOWER(CONCAT('%', :query, '%'))) " +
       "AND (:roleType IS NULL OR r.roleType = :roleType) " +
       "AND (:status IS NULL OR :status = '' " +
       "OR LOWER(r.status) = LOWER(:status))")
List<Role> searchRoles(
        @Param("tenantId") String tenantId,
        @Param("query") String query,
        @Param("roleType") RoleType roleType,
        @Param("status") String status);

long countByTenantIdAndIsDeletedFalse(String tenantId);
long countByTenantIdAndRoleTypeAndIsDeletedFalse(String tenantId, RoleType roleType);

    // Every tenant-facing query goes through tenantId — never fetch by id alone
    // for anything the caller could see, to keep cross-tenant leaks impossible.
    Optional<Role> findByIdAndTenantId(String id, String tenantId);

    List<Role> findAllByTenantId(String tenantId);

    List<Role> findAllByTenantIdAndType(String tenantId, RoleType type);

    Optional<Role> findByTenantIdAndRoleCode(String tenantId, String roleCode);

    boolean existsByTenantIdAndRoleCode(String tenantId, String roleCode);
}