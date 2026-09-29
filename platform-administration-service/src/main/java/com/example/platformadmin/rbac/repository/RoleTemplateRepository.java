package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.RoleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, UUID> {

    @Query("SELECT DISTINCT t FROM RoleTemplate t LEFT JOIN FETCH t.permissions")
    List<RoleTemplate> findAllWithPermissions();

    @Query("SELECT DISTINCT t FROM RoleTemplate t LEFT JOIN FETCH t.permissions WHERE t.hidden = false")
    List<RoleTemplate> findAllByHiddenFalse();
}
