package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.request.CreateDataAccessRuleRequest;
import com.example.platformadmin.rbac.dto.request.RuleTestRequest;
import com.example.platformadmin.rbac.dto.request.UpdateDataAccessRuleRequest;
import com.example.platformadmin.rbac.dto.response.DataAccessRuleResponse;
import com.example.platformadmin.rbac.dto.response.DataPermissionReportResponse;
import com.example.platformadmin.rbac.dto.response.RuleTestResponse;

import java.util.List;
import java.util.UUID;

public interface DataAccessRuleService {

    DataAccessRuleResponse create(CreateDataAccessRuleRequest request);

    DataAccessRuleResponse update(UUID ruleId, UpdateDataAccessRuleRequest request);

    void delete(UUID ruleId);

    DataAccessRuleResponse getById(UUID ruleId);

    List<DataAccessRuleResponse> getRules();

    List<DataAccessRuleResponse> getRulesByRole(UUID roleId);

    RuleTestResponse testRule(RuleTestRequest request);

    List<DataPermissionReportResponse> getReport();
}