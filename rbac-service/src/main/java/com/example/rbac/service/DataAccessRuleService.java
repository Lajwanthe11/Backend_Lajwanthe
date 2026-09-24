package com.example.rbac.service;

import com.example.rbac.dto.request.CreateDataAccessRuleRequest;
import com.example.rbac.dto.request.RuleTestRequest;
import com.example.rbac.dto.request.UpdateDataAccessRuleRequest;
import com.example.rbac.dto.response.DataAccessRuleResponse;
import com.example.rbac.dto.response.DataPermissionReportResponse;
import com.example.rbac.dto.response.RuleTestResponse;

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