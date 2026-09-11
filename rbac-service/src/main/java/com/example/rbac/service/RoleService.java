package com.example.rbac.service;

import com.example.common.abstracts.BaseService;
import com.example.rbac.dto.*;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import java.util.List;
import java.util.Map;

//Service contract for Role operations
public interface RoleService extends BaseService<Role, Long, RoleRequestDto, RoleResponseDto> {

//Service roles by name or code
List<RoleResponseDto> searchRoles(String query, RoleType roleType, String status);
 // Activate or deactivate role
RoleResponseDto updateStatus(Long id, String status);

Map<String, Long> getRoleCounts();

 List<RoleTemplateSummaryDto> listTemplates();

 RoleTemplateDetailDto getTemplateDetail(String templateId);

 RoleCompareResponse compareRoles(String role1Id, String role2Id);

 List<RoleResponseDto> listSystemRoles();

 RoleResponseDto cloneRole(String sourceRoleId, RoleCloneRequest request);

 List<RoleHistoryDto> getHistory(String roleId);

 byte[] exportRoles(String format);

}