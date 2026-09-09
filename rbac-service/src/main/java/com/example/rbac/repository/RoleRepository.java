package com.example.rbac.repository;

import com.example.rbac.entity.Role;
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
       "AND (LOWER(r.roleName) LIKE LOWER(CONCAT('%', :query, '%')) " +
       "OR LOWER(r.roleCode) LIKE LOWER(CONCAT('%', :query, '%')))")
List<Role> searchRoles(
        @Param("tenantId") String tenantId,
        @Param("query") String query);
}