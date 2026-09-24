package com.example.rbac.controller;

import com.example.common.response.ApiResponse;
import com.example.rbac.config.PublicEndpoint;
import com.example.rbac.config.RequirePermission;
import com.example.rbac.dto.request.CreateDataAccessRuleRequest;
import com.example.rbac.dto.request.RuleTestRequest;
import com.example.rbac.dto.request.UpdateDataAccessRuleRequest;
import com.example.rbac.dto.response.DataAccessRuleResponse;
import com.example.rbac.dto.response.DataPermissionReportResponse;
import com.example.rbac.dto.response.RuleTestResponse;
import com.example.rbac.service.DataAccessRuleService;
import com.example.rbac.service.DataPermissionEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data-permissions")
@RequiredArgsConstructor
@Tag(name = "Data Permissions", description = "Endpoints for managing Row-Level and Field-Level Data Access Rules")
public class DataAccessRuleController {

    private final DataAccessRuleService dataAccessRuleService;
    private final DataPermissionEvaluationService evaluationService;

    @RequirePermission("DATA_RULE_READ")
    @GetMapping("/rules")
    @Operation(summary = "List all data access rules", description = "Retrieves all active data access rules for the caller's tenant")
    public ResponseEntity<ApiResponse<List<DataAccessRuleResponse>>> listRules() {
        List<DataAccessRuleResponse> rules = dataAccessRuleService.getRules();
        return ResponseEntity.ok(ApiResponse.ok(rules));
    }

    @RequirePermission("DATA_RULE_WRITE")
    @PostMapping("/rules")
    @Operation(summary = "Create a new data access rule", description = "Defines a row-level or field-level data access rule for a role")
    public ResponseEntity<ApiResponse<DataAccessRuleResponse>> createRule(
            @Valid @RequestBody CreateDataAccessRuleRequest request) {
        DataAccessRuleResponse response = dataAccessRuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Data access rule created successfully", response));
    }

    @RequirePermission("DATA_RULE_WRITE")
    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "Update a data access rule", description = "Updates an existing data access rule by ID")
    public ResponseEntity<ApiResponse<DataAccessRuleResponse>> updateRule(
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateDataAccessRuleRequest request) {
        DataAccessRuleResponse response = dataAccessRuleService.update(ruleId, request);
        return ResponseEntity.ok(ApiResponse.ok("Data access rule updated successfully", response));
    }

    @RequirePermission("DATA_RULE_WRITE")
    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "Delete a data access rule", description = "Deletes a data access rule by ID")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable UUID ruleId) {
        dataAccessRuleService.delete(ruleId);
        return ResponseEntity.ok(ApiResponse.ok("Data access rule deleted successfully", null));
    }

    @RequirePermission("DATA_RULE_READ")
    @PostMapping("/rules/test")
    @Operation(summary = "Test a rule simulation", description = "Simulates what data a user or role can access in read-only mode")
    public ResponseEntity<ApiResponse<RuleTestResponse>> testRule(
            @Valid @RequestBody RuleTestRequest request) {
        RuleTestResponse response = dataAccessRuleService.testRule(request);
        return ResponseEntity.ok(ApiResponse.ok("Rule simulation completed", response));
    }

    @RequirePermission("DATA_RULE_READ")
    @GetMapping("/rules/by-role/{id}")
    @Operation(summary = "Get all data rules for a specific role", description = "Retrieves all data access rules associated with a role ID")
    public ResponseEntity<ApiResponse<List<DataAccessRuleResponse>>> getRulesByRole(@PathVariable("id") UUID id) {
        List<DataAccessRuleResponse> rules = dataAccessRuleService.getRulesByRole(id);
        return ResponseEntity.ok(ApiResponse.ok(rules));
    }

    @PublicEndpoint(reason = "Query effective data permissions for current context")
    @GetMapping("/apply")
    @Operation(summary = "Apply rules for current user", description = "Returns effective data access permissions and restrictions for the authenticated user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> applyRules() {
        Map<String, Object> effectiveRules = evaluationService.getEffectivePermissionsSummary(null);
        return ResponseEntity.ok(ApiResponse.ok(effectiveRules));
    }

    @RequirePermission("DATA_RULE_READ")
    @GetMapping("/report")
    @Operation(summary = "Data permission report", description = "Generates a comprehensive summary report of data permissions by role")
    public ResponseEntity<ApiResponse<List<DataPermissionReportResponse>>> getReport() {
        List<DataPermissionReportResponse> report = dataAccessRuleService.getReport();
        return ResponseEntity.ok(ApiResponse.ok(report));
    }
}
