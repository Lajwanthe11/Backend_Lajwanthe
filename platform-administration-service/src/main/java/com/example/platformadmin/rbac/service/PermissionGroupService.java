package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.response.PermissionGroupResponseDto;

import java.util.List;
import java.util.UUID;

public interface PermissionGroupService {

    List<PermissionGroupResponseDto> listGroups();

    PermissionGroupResponseDto getGroupWithPermissions(UUID groupId);
}
