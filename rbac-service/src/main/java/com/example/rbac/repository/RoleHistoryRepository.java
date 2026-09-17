package com.example.rbac.repository;

import com.example.rbac.entity.RoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleHistoryRepository extends JpaRepository<RoleHistory, String> {
    List<RoleHistory> findAllByRoleIdOrderByChangedAtDesc(String roleId);

}