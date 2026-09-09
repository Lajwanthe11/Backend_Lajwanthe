package com.example.rbac.service;

import com.example.common.abstracts.BaseService;
import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.entity.Role;

import java.util.List;

//Service contract for Role operations
public interface RoleService extends BaseService<Role, Long, RoleRequestDto, RoleResponseDto> {

//Service roles by name or code
   List<RoleResponseDto> searchRoles(String query);
 // Activate or deactivate role
   RoleResponseDto updateStatus(Long id, String status);
}