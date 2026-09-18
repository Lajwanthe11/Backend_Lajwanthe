package com.example.rbac.service;

import com.example.rbac.dto.BatchPermissionUpdateRequest;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.dto.PermissionGrantRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for processing multiple permission changes
 * for a role in a single batch operation.
 *
 * This is used by the Permission Matrix Save operation.
 */
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

    /**
     * Updates multiple permissions for a role.
     *
     * Each item in the request specifies:
     * - which permission is being changed
     * - whether the permission should be granted or revoked
     *
     * The complete batch runs inside one transaction.
     */
    @Transactional
    public BatchPermissionUpdateResponse updatePermissions(
            UUID roleId,
            BatchPermissionUpdateRequest request,
            UUID userId) {

        // Verify that the role exists before processing
        // the permission updates.
        Role role = roleRepository.findById(roleId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Role not found"));

        int updatedCount = 0;

        // Process every permission update supplied by the client.
        for (PermissionGrantRequest permission : request.getPermissions()) {

            /*
             * Boolean.TRUE.equals() safely checks the Boolean value.
             *
             * true = grant permission
             * false = revoke permission
             */
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

        // Store the user who performed the batch update
        // on the role's update information.
        role.setUpdatedBy(userId);

        roleRepository.saveAndFlush(role);

        // Return the result of the batch operation.
        return new BatchPermissionUpdateResponse(
                roleId,
                updatedCount,
                "Permissions updated successfully");
    }
}
