// CustomRoleService.java

package com.example.rbac.service;

import com.example.rbac.dto.CustomRoleRequest;
import com.example.rbac.dto.CustomRoleResponse;

import java.util.List;

public interface CustomRoleService {

    CustomRoleResponse create(CustomRoleRequest request);

    List<CustomRoleResponse> getAll();

    CustomRoleResponse update(Long roleId, CustomRoleRequest request);

    CustomRoleResponse publish(Long roleId, String publishNotes);

    CustomRoleResponse archive(Long roleId);

    List<CustomRoleResponse> getVersions(Long roleId);

    CustomRoleResponse revert(Long roleId, Integer version);

    Object getImpact(Long roleId);

    Object getLimits();
}