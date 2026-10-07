package com.example.platformadmin.rbac.service;


import com.example.platformadmin.rbac.dto.request.AssignRoleRequest;
import com.example.platformadmin.rbac.dto.request.RevokeRoleRequest;
import com.example.platformadmin.rbac.dto.response.UserRoleResponse;

import java.util.List;
import java.util.UUID;

public interface UserRoleService {

    List<UserRoleResponse> assignRoles(
            UUID userId,
            AssignRoleRequest request
    );

    List<UserRoleResponse> getCurrentRoles(
            UUID userId
    );

    void revokeRole(
            UUID userId,
            UUID roleId,
            RevokeRoleRequest request
    );

    void setPrimaryRole(
            UUID userId,
            UUID roleId
    );

    List<UserRoleResponse> getRoleHistory(
            UUID userId
    );

    List<UserRoleResponse> getUsersByRole(
            UUID roleId
    );
}