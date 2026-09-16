package com.example.rbac.service;

import com.example.rbac.dto.BatchPermissionUpdateRequest;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.dto.PermissionGrantRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.repository.RoleRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RolePermissionBatchService {

    private final RoleRepository roleRepository;
    private final RolePermissionService rolePermissionService;

    public RolePermissionBatchService(
            RoleRepository roleRepository,
            RolePermissionService rolePermissionService) {
        this.roleRepository = roleRepository;
        this.rolePermissionService = rolePermissionService;
    }

    @Transactional
    public BatchPermissionUpdateResponse updatePermissions(
            UUID roleId,
            BatchPermissionUpdateRequest request,
            String userId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Long currentVersion = role.getVersion();

        if (!request.getExpectedVersion().equals(currentVersion)) {
            throw new ObjectOptimisticLockingFailureException(
                    Role.class,
                    roleId);
        }

        int updatedCount = 0;

        for (PermissionGrantRequest permission : request.getPermissions()) {

            if (Boolean.TRUE.equals(permission.getGranted())) {

                rolePermissionService.grantPermission(
                        roleId,
                        permission.getPermissionId(),
                        userId);

            } else {

                rolePermissionService.revokePermission(
                        roleId,
                        permission.getPermissionId(),
                        userId);
            }

            updatedCount++;
        }

        role.setUpdatedBy(userId);

        try {
            roleRepository.saveAndFlush(role);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ObjectOptimisticLockingFailureException(
                    Role.class,
                    roleId);
        }

        return new BatchPermissionUpdateResponse(
                roleId,
                updatedCount,
                "Permissions updated successfully");
    }
}