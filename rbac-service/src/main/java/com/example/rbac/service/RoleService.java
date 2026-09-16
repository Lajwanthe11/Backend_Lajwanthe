package com.example.rbac.service;

import com.example.common.abstracts.BaseService;
import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RoleService extends BaseService<Role, UUID, RoleRequestDto, RoleResponseDto> {

    List<RoleResponseDto> searchRoles( String query, RoleType roleType, String status);

    RoleResponseDto updateStatus(UUID id, String status);

    Map<String, Long> getRoleCounts();
}