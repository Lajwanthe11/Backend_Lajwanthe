package com.example.rbac.repository;

import com.example.rbac.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, UUID> {

    Optional<PermissionGroup> findByGroupCode(String groupCode);

    List<PermissionGroup> findByModuleIgnoreCaseAndActiveTrueOrderByDisplayOrderAsc(String module);

    List<PermissionGroup> findAllByOrderByDisplayOrderAsc();
}
