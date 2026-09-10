package com.example.rbac.service;


import com.example.rbac.dto.request.AssignRoleRequest;
import com.example.rbac.dto.request.RevokeRoleRequest;
import com.example.rbac.dto.response.UserRoleResponse;

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