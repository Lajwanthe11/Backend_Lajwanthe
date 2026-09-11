package com.example.rbac.repository;

import com.example.rbac.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByPermissionCode(String permissionCode);

    List<Permission> findByGroup_GroupIdAndActiveTrue(UUID groupId);

    List<Permission> findByModuleIgnoreCaseAndActiveTrue(String module);

    @Query("""
            SELECT p FROM Permission p
            WHERE (:groupId IS NULL OR p.group.groupId = :groupId)
              AND (:module IS NULL OR LOWER(p.module) = LOWER(:module))
              AND (:activeOnly = false OR p.active = true)
            """)
    Page<Permission> filter(@Param("groupId") UUID groupId,
                             @Param("module") String module,
                             @Param("activeOnly") boolean activeOnly,
                             Pageable pageable);

    @Query("""
            SELECT p FROM Permission p
            WHERE LOWER(p.permissionCode) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(p.displayName) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(p.module) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    List<Permission> search(@Param("term") String term);

    List<Permission> findByCodeIn(List<String> codes);
}
