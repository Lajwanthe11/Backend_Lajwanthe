package com.example.rbac.service;

import com.example.common.abstracts.BaseService;
import com.example.rbac.dto.*;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RoleService extends BaseService<Role, UUID, RoleRequestDto, RoleResponseDto> {

    List<RoleResponseDto> searchRoles( String query, RoleType roleType, String status);

    RoleResponseDto updateStatus(UUID id, String status);

    Map<String, Long> getRoleCounts();

    List<RoleTemplateSummaryDto> listTemplates();

    RoleTemplateDetailDto getTemplateDetail(UUID templateId);

    void updateTemplateVisibility(UUID templateId, boolean hidden);

    List<RoleResponseDto> listSystemRoles();

    RoleResponseDto cloneRole(UUID roleId, RoleCloneRequest request);

    RoleCompareResponse compareRoles(UUID role1Id, UUID role2Id);

    List<RoleHistoryDto> getHistory(UUID roleId);

    byte[] exportRoles(String format);


}