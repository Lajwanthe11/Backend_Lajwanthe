package com.example.rbac.repository;

import com.example.rbac.entity.RoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RoleHistoryRepository extends JpaRepository<RoleHistory, UUID> {
    List<RoleHistory> findAllByRoleIdOrderByChangedAtDesc(UUID roleId);
}