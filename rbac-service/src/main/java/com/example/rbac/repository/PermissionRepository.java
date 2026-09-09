package com.example.rbac.repository;

import com.example.rbac.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PermissionRepository
        extends JpaRepository<Permission, UUID> {

    List<Permission> findByActiveTrueOrderByGroup_DisplayOrderAscDisplayNameAsc();
}