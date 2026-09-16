// CustomRoleService.java

package com.example.rbac.service;

import com.example.rbac.dto.CustomRoleRequest;
import com.example.rbac.dto.CustomRoleResponse;

import java.util.List;import java.util.UUID;

public interface CustomRoleService {

    CustomRoleResponse create(CustomRoleRequest request);

    List<CustomRoleResponse> getAll();

    CustomRoleResponse update(UUID roleId, CustomRoleRequest request);

    CustomRoleResponse publish(UUID roleId, String publishNotes);

    CustomRoleResponse archive(UUID roleId);

    List<CustomRoleResponse> getVersions(UUID roleId);

    CustomRoleResponse revert(UUID roleId, Integer version);

    Object getImpact(UUID roleId);

    Object getLimits();
}