package com.example.rbac.repository;

import com.example.rbac.entity.RoleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, UUID> {

    // Library view for Org Admin / general use — hidden templates excluded
    @Query("""
        SELECT DISTINCT t
        FROM RoleTemplate t
        LEFT JOIN FETCH t.permissions
        """)
    List<RoleTemplate> findAllWithPermissions();

    @Query("""
        SELECT DISTINCT t
        FROM RoleTemplate t
        LEFT JOIN FETCH t.permissions
        WHERE t.hidden = false
        """)
    List<RoleTemplate> findAllByHiddenFalse();

    // Super Admin management view — sees everything, hidden or not
    // (use findAll() directly for that case)


}
