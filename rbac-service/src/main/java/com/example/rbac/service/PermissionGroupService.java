package com.example.rbac.service;

import com.example.rbac.dto.PermissionGroupResponseDto;

import java.util.List;
import java.util.UUID;

public interface PermissionGroupService {

    List<PermissionGroupResponseDto> listGroups();

    PermissionGroupResponseDto getGroupWithPermissions(UUID groupId);
}
