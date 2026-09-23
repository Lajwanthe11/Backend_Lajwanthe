package com.example.rbac.repository;

import com.example.rbac.entity.RoleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, UUID> {

    // Library view for Org Admin / general use — hidden templates excluded
    List<RoleTemplate> findAllByHiddenFalse();

    // Super Admin management view — sees everything, hidden or not
    // (use findAll() directly for that case)
}
