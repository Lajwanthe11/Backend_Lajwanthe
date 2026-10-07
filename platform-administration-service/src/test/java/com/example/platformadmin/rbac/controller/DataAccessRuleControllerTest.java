package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.dto.request.CreateDataAccessRuleRequest;
import com.example.platformadmin.rbac.dto.request.RuleTestRequest;
import com.example.platformadmin.rbac.dto.request.UpdateDataAccessRuleRequest;
import com.example.platformadmin.rbac.dto.response.DataAccessRuleResponse;
import com.example.platformadmin.rbac.dto.response.DataPermissionReportResponse;
import com.example.platformadmin.rbac.dto.response.RuleTestResponse;
import com.example.platformadmin.rbac.enums.ConditionOperator;
import com.example.platformadmin.rbac.enums.RuleType;
import com.example.platformadmin.rbac.service.DataAccessRuleService;
import com.example.platformadmin.rbac.service.serviceImpl.DataPermissionEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataAccessRuleController.class)
@AutoConfigureMockMvc(addFilters = false)
class DataAccessRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DataAccessRuleService dataAccessRuleService;

    @MockitoBean
    private DataPermissionEvaluationService evaluationService;

    private static final UUID RULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TENANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ROLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private DataAccessRuleResponse sampleRowRuleResponse() {
        return new DataAccessRuleResponse(
                RULE_ID,
                TENANT_ID,
                ROLE_ID,
                "ORDER",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.EQUALS,
                "FINANCE",
                null,
                null,
                true,
                10,
                USER_ID,
                LocalDateTime.now()
        );
    }

    private DataAccessRuleResponse sampleFieldRuleResponse() {
        return new DataAccessRuleResponse(
                UUID.randomUUID(),
                TENANT_ID,
                ROLE_ID,
                "EMPLOYEE",
                RuleType.FIELD_LEVEL,
                null,
                null,
                null,
                new String[]{"id", "name", "department"},
                new String[]{"salary", "ssn"},
                true,
                5,
                USER_ID,
                LocalDateTime.now()
        );
    }

    // ============================================================
    // 1. GET /api/v1/data-permissions/rules
    // ============================================================

    @Test
    @DisplayName("GET /rules - Should return list of data access rules")
    void listRules_shouldReturnRulesList() throws Exception {
        DataAccessRuleResponse rule = sampleRowRuleResponse();
        when(dataAccessRuleService.getRules()).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/v1/data-permissions/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ruleId").value(RULE_ID.toString()))
                .andExpect(jsonPath("$.data[0].resourceType").value("ORDER"))
                .andExpect(jsonPath("$.data[0].ruleType").value("ROW_LEVEL"))
                .andExpect(jsonPath("$.data[0].conditionField").value("departmentId"))
                .andExpect(jsonPath("$.data[0].conditionOperator").value("EQUALS"))
                .andExpect(jsonPath("$.data[0].conditionValue").value("FINANCE"));

        verify(dataAccessRuleService).getRules();
    }

    @Test
    @DisplayName("GET /rules - Should return empty list when no rules exist")
    void listRules_whenNoRulesExist_shouldReturnEmptyList() throws Exception {
        when(dataAccessRuleService.getRules()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/data-permissions/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(dataAccessRuleService).getRules();
    }

    // ============================================================
    // 2. POST /api/v1/data-permissions/rules
    // ============================================================

    @Test
    @DisplayName("POST /rules - Should create row-level rule and return 201 Created")
    void createRule_rowLevel_shouldReturn201Created() throws Exception {
        CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                ROLE_ID,
                "ORDER",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.EQUALS,
                "FINANCE",
                null,
                null,
                10
        );
        DataAccessRuleResponse response = sampleRowRuleResponse();
        when(dataAccessRuleService.create(any(CreateDataAccessRuleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Data access rule created successfully"))
                .andExpect(jsonPath("$.data.ruleId").value(RULE_ID.toString()))
                .andExpect(jsonPath("$.data.resourceType").value("ORDER"))
                .andExpect(jsonPath("$.data.ruleType").value("ROW_LEVEL"));

        verify(dataAccessRuleService).create(any(CreateDataAccessRuleRequest.class));
    }

    @Test
    @DisplayName("POST /rules - Should create field-level rule and return 201 Created")
    void createRule_fieldLevel_shouldReturn201Created() throws Exception {
        CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                ROLE_ID,
                "EMPLOYEE",
                RuleType.FIELD_LEVEL,
                null,
                null,
                null,
                new String[]{"id", "name"},
                new String[]{"salary"},
                5
        );
        DataAccessRuleResponse response = sampleFieldRuleResponse();
        when(dataAccessRuleService.create(any(CreateDataAccessRuleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ruleType").value("FIELD_LEVEL"))
                .andExpect(jsonPath("$.data.allowedFields[0]").value("id"))
                .andExpect(jsonPath("$.data.deniedFields[0]").value("salary"));

        verify(dataAccessRuleService).create(any(CreateDataAccessRuleRequest.class));
    }

    @Test
    @DisplayName("POST /rules - Should return 400 Bad Request when roleId is null")
    void createRule_missingRoleId_shouldReturn400BadRequest() throws Exception {
        CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                null,
                "ORDER",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.EQUALS,
                "FINANCE",
                null,
                null,
                0
        );

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dataAccessRuleService, never()).create(any());
    }

    @Test
    @DisplayName("POST /rules - Should return 400 Bad Request when resourceType is blank")
    void createRule_blankResourceType_shouldReturn400BadRequest() throws Exception {
        CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                ROLE_ID,
                "   ",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.EQUALS,
                "FINANCE",
                null,
                null,
                0
        );

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dataAccessRuleService, never()).create(any());
    }

    @Test
    @DisplayName("POST /rules - Should return 400 Bad Request when ruleType is null")
    void createRule_nullRuleType_shouldReturn400BadRequest() throws Exception {
        String invalidJson = """
                {
                    "roleId": "%s",
                    "resourceType": "ORDER",
                    "ruleType": null
                }
                """.formatted(ROLE_ID);

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(dataAccessRuleService, never()).create(any());
    }

    @Test
    @DisplayName("POST /rules - Should return 400 Bad Request when priority is negative")
    void createRule_negativePriority_shouldReturn400BadRequest() throws Exception {
        CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                ROLE_ID,
                "ORDER",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.EQUALS,
                "FINANCE",
                null,
                null,
                -1
        );

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dataAccessRuleService, never()).create(any());
    }

    // ============================================================
    // 3. PUT /api/v1/data-permissions/rules/{ruleId}
    // ============================================================

    @Test
    @DisplayName("PUT /rules/{ruleId} - Should update rule and return 200 OK")
    void updateRule_shouldReturn200Ok() throws Exception {
        UpdateDataAccessRuleRequest request = new UpdateDataAccessRuleRequest(
                ROLE_ID,
                "ORDER",
                RuleType.ROW_LEVEL,
                "region",
                ConditionOperator.IN,
                "EU,US",
                null,
                null,
                20,
                true
        );
        DataAccessRuleResponse response = new DataAccessRuleResponse(
                RULE_ID,
                TENANT_ID,
                ROLE_ID,
                "ORDER",
                RuleType.ROW_LEVEL,
                "region",
                ConditionOperator.IN,
                "EU,US",
                null,
                null,
                true,
                20,
                USER_ID,
                LocalDateTime.now()
        );
        when(dataAccessRuleService.update(eq(RULE_ID), any(UpdateDataAccessRuleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/data-permissions/rules/{ruleId}", RULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Data access rule updated successfully"))
                .andExpect(jsonPath("$.data.ruleId").value(RULE_ID.toString()))
                .andExpect(jsonPath("$.data.conditionField").value("region"))
                .andExpect(jsonPath("$.data.conditionOperator").value("IN"))
                .andExpect(jsonPath("$.data.priority").value(20));

        verify(dataAccessRuleService).update(eq(RULE_ID), any(UpdateDataAccessRuleRequest.class));
    }

    // ============================================================
    // 4. DELETE /api/v1/data-permissions/rules/{ruleId}
    // ============================================================

    @Test
    @DisplayName("DELETE /rules/{ruleId} - Should delete rule and return 200 OK")
    void deleteRule_shouldReturn200Ok() throws Exception {
        doNothing().when(dataAccessRuleService).delete(RULE_ID);

        mockMvc.perform(delete("/api/v1/data-permissions/rules/{ruleId}", RULE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Data access rule deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(dataAccessRuleService).delete(RULE_ID);
    }

    // ============================================================
    // 5. POST /api/v1/data-permissions/rules/test
    // ============================================================

    @Test
    @DisplayName("POST /rules/test - Should simulate rule test and return 200 OK")
    void testRule_shouldReturnSimulationResult() throws Exception {
        RuleTestRequest request = new RuleTestRequest(
                USER_ID,
                ROLE_ID,
                "ORDER",
                Map.of("id", 101, "departmentId", "FINANCE", "amount", 5000),
                Map.of("departmentId", "FINANCE")
        );

        RuleTestResponse response = new RuleTestResponse(
                true,
                null,
                new String[]{"id", "departmentId", "amount"},
                new String[0],
                Map.of("id", 101, "departmentId", "FINANCE", "amount", 5000),
                List.of(sampleRowRuleResponse())
        );

        when(dataAccessRuleService.testRule(any(RuleTestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/data-permissions/rules/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rule simulation completed"))
                .andExpect(jsonPath("$.data.rowAccessAllowed").value(true))
                .andExpect(jsonPath("$.data.filteredData.departmentId").value("FINANCE"));

        verify(dataAccessRuleService).testRule(any(RuleTestRequest.class));
    }

    @Test
    @DisplayName("POST /rules/test - Should return 400 Bad Request when resourceType is blank")
    void testRule_blankResourceType_shouldReturn400BadRequest() throws Exception {
        RuleTestRequest request = new RuleTestRequest(
                USER_ID,
                ROLE_ID,
                "",
                Map.of("id", 1),
                Map.of()
        );

        mockMvc.perform(post("/api/v1/data-permissions/rules/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dataAccessRuleService, never()).testRule(any());
    }

    // ============================================================
    // 6. GET /api/v1/data-permissions/rules/by-role/{id}
    // ============================================================

    @Test
    @DisplayName("GET /rules/by-role/{id} - Should return rules for specified role")
    void getRulesByRole_shouldReturnRulesList() throws Exception {
        DataAccessRuleResponse rule = sampleRowRuleResponse();
        when(dataAccessRuleService.getRulesByRole(ROLE_ID)).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/v1/data-permissions/rules/by-role/{id}", ROLE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].roleId").value(ROLE_ID.toString()));

        verify(dataAccessRuleService).getRulesByRole(ROLE_ID);
    }

    @Test
    @DisplayName("GET /rules/by-role/{id} - Should return empty list when role has no rules")
    void getRulesByRole_whenNoRules_shouldReturnEmptyList() throws Exception {
        when(dataAccessRuleService.getRulesByRole(ROLE_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/data-permissions/rules/by-role/{id}", ROLE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(dataAccessRuleService).getRulesByRole(ROLE_ID);
    }

    // ============================================================
    // 7. GET /api/v1/data-permissions/apply
    // ============================================================

    @Test
    @DisplayName("GET /apply - Should return effective permissions summary")
    void applyRules_shouldReturnEffectivePermissions() throws Exception {
        Map<String, Object> summary = Map.of(
                "userId", USER_ID.toString(),
                "tenantId", TENANT_ID.toString(),
                "isSuperAdmin", false,
                "allFieldsAllowed", false,
                "activeRulesCount", 2
        );

        when(evaluationService.getEffectivePermissionsSummary(null)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/data-permissions/apply"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.data.activeRulesCount").value(2));

        verify(evaluationService).getEffectivePermissionsSummary(null);
    }

    // ============================================================
    // 8. GET /api/v1/data-permissions/report
    // ============================================================

    @Test
    @DisplayName("GET /report - Should return data permission report")
    void getReport_shouldReturnReportList() throws Exception {
        DataPermissionReportResponse reportItem = new DataPermissionReportResponse(
                ROLE_ID,
                "Role-33333333",
                "ORDER",
                1L,
                1L,
                0L,
                List.of(sampleRowRuleResponse())
        );

        when(dataAccessRuleService.getReport()).thenReturn(List.of(reportItem));

        mockMvc.perform(get("/api/v1/data-permissions/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].roleId").value(ROLE_ID.toString()))
                .andExpect(jsonPath("$.data[0].resourceType").value("ORDER"))
                .andExpect(jsonPath("$.data[0].totalRules").value(1))
                .andExpect(jsonPath("$.data[0].rowLevelRulesCount").value(1))
                .andExpect(jsonPath("$.data[0].fieldLevelRulesCount").value(0));

        verify(dataAccessRuleService).getReport();
    }
}
