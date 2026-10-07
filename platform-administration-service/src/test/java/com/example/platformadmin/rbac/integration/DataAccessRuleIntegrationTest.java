package com.example.platformadmin.rbac.integration;

import com.example.platformadmin.rbac.controller.IntegrationTestConfig;
import org.springframework.context.annotation.Import;
import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.dto.request.CreateDataAccessRuleRequest;
import com.example.platformadmin.rbac.dto.request.RuleTestRequest;
import com.example.platformadmin.rbac.dto.request.UpdateDataAccessRuleRequest;
import com.example.platformadmin.rbac.entity.DataAccessRule;
import com.example.platformadmin.rbac.enums.ConditionOperator;
import com.example.platformadmin.rbac.enums.RuleType;
import com.example.platformadmin.rbac.repository.DataAccessRuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionAuthorizationService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(IntegrationTestConfig.class)
class DataAccessRuleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataAccessRuleRepository repository;

    @MockitoBean
    private PermissionAuthorizationService permissionAuthorizationService;

    private final UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID roleId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId.toString());
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
        TenantContext.clear();
    }

    // ============================================================
    // 1. Full CRUD Lifecycle: Create, List, GetByRole, Update, Delete
    // ============================================================

    @Test
    @DisplayName("Lifecycle: Create, Read, Update, and Delete ROW_LEVEL Data Access Rule")
    void rowLevelRule_fullCrudLifecycle() throws Exception {
        // Step 1: Create ROW_LEVEL rule
        CreateDataAccessRuleRequest createRequest = new CreateDataAccessRuleRequest(
                roleId,
                "ORDER",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.EQUALS,
                "FINANCE",
                null,
                null,
                10
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ruleId").exists())
                .andExpect(jsonPath("$.data.resourceType").value("ORDER"))
                .andExpect(jsonPath("$.data.ruleType").value("ROW_LEVEL"))
                .andExpect(jsonPath("$.data.conditionOperator").value("EQUALS"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID createdRuleId = UUID.fromString(createdJson.path("data").path("ruleId").asText());

        // Step 2: Verify rule exists in DB
        Optional<DataAccessRule> persisted = repository.findById(createdRuleId);
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getResourceType()).isEqualTo("ORDER");
        assertThat(persisted.get().getConditionValue()).isEqualTo("FINANCE");

        // Step 3: GET all rules for tenant
        mockMvc.perform(get("/api/v1/data-permissions/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ruleId").value(createdRuleId.toString()));

        // Step 4: GET rules by role
        mockMvc.perform(get("/api/v1/data-permissions/rules/by-role/{id}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].roleId").value(roleId.toString()));

        // Step 5: UPDATE the rule
        UpdateDataAccessRuleRequest updateRequest = new UpdateDataAccessRuleRequest(
                roleId,
                "ORDER",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.IN,
                "FINANCE,LEGAL",
                null,
                null,
                25,
                true
        );

        mockMvc.perform(put("/api/v1/data-permissions/rules/{ruleId}", createdRuleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.conditionOperator").value("IN"))
                .andExpect(jsonPath("$.data.conditionValue").value("FINANCE,LEGAL"))
                .andExpect(jsonPath("$.data.priority").value(25));

        // Verify update in DB
        DataAccessRule updatedRule = repository.findById(createdRuleId).orElseThrow();
        assertThat(updatedRule.getConditionOperator()).isEqualTo(ConditionOperator.IN);
        assertThat(updatedRule.getConditionValue()).isEqualTo("FINANCE,LEGAL");
        assertThat(updatedRule.getPriority()).isEqualTo(25);

        // Step 6: DELETE the rule
        mockMvc.perform(delete("/api/v1/data-permissions/rules/{ruleId}", createdRuleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 7: Verify rule is deleted from DB
        assertThat(repository.findById(createdRuleId)).isEmpty();

        // Step 8: GET rules should now be empty
        mockMvc.perform(get("/api/v1/data-permissions/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ============================================================
    // 2. Field-Level Rule Integration Flow
    // ============================================================

    @Test
    @DisplayName("Field-Level: Create and Retrieve FIELD_LEVEL Data Access Rule")
    void fieldLevelRule_createAndRetrieve() throws Exception {
        CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                roleId,
                "EMPLOYEE",
                RuleType.FIELD_LEVEL,
                null,
                null,
                null,
                new String[]{"id", "fullName", "email"},
                new String[]{"salary", "ssn"},
                5
        );

        MvcResult result = mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ruleType").value("FIELD_LEVEL"))
                .andExpect(jsonPath("$.data.allowedFields.length()").value(3))
                .andExpect(jsonPath("$.data.deniedFields.length()").value(2))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID ruleId = UUID.fromString(json.path("data").path("ruleId").asText());

        Optional<DataAccessRule> persisted = repository.findById(ruleId);
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getAllowedFields()).containsExactly("id", "fullName", "email");
        assertThat(persisted.get().getDeniedFields()).containsExactly("salary", "ssn");
    }

    // ============================================================
    // 3. Rule Simulation Test Flow
    // ============================================================

    @Test
    @DisplayName("Simulation: Simulate rule test via /rules/test endpoint")
    void simulateRuleTest_shouldReturnValidResponse() throws Exception {
        // Pre-seed a row level rule
        CreateDataAccessRuleRequest createRule = new CreateDataAccessRuleRequest(
                roleId,
                "ORDER",
                RuleType.ROW_LEVEL,
                "departmentId",
                ConditionOperator.EQUALS,
                "FINANCE",
                null,
                null,
                1
        );

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRule)))
                .andExpect(status().isCreated());

        RuleTestRequest testRequest = new RuleTestRequest(
                UUID.randomUUID(),
                roleId,
                "ORDER",
                Map.of("id", 1, "departmentId", "FINANCE", "amount", 500),
                Map.of("departmentId", "FINANCE")
        );

        mockMvc.perform(post("/api/v1/data-permissions/rules/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rowAccessAllowed").value(true));
    }

    // ============================================================
    // 4. Data Permission Report Flow
    // ============================================================

    @Test
    @DisplayName("Report: Generate summary report for data access rules")
    void getReport_shouldAggregateRuleCounts() throws Exception {
        // Create 1 ROW_LEVEL and 1 FIELD_LEVEL rule for the role
        CreateDataAccessRuleRequest rowRule = new CreateDataAccessRuleRequest(
                roleId, "ORDER", RuleType.ROW_LEVEL, "dept", ConditionOperator.EQUALS, "IT", null, null, 1);
        CreateDataAccessRuleRequest fieldRule = new CreateDataAccessRuleRequest(
                roleId, "ORDER", RuleType.FIELD_LEVEL, null, null, null, new String[]{"id"}, null, 2);

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rowRule)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fieldRule)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/data-permissions/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].roleId").value(roleId.toString()))
                .andExpect(jsonPath("$.data[0].resourceType").value("ORDER"))
                .andExpect(jsonPath("$.data[0].totalRules").value(2))
                .andExpect(jsonPath("$.data[0].rowLevelRulesCount").value(1))
                .andExpect(jsonPath("$.data[0].fieldLevelRulesCount").value(1));
    }

    // ============================================================
    // 5. Apply Rules Flow
    // ============================================================

    @Test
    @DisplayName("Apply: /apply endpoint should return effective permissions")
    void applyRules_shouldReturnEffectiveRulesSummary() throws Exception {
        mockMvc.perform(get("/api/v1/data-permissions/apply"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    // ============================================================
    // 6. Validation Error Flows
    // ============================================================

    @Test
    @DisplayName("Validation: POST /rules with missing roleId returns 400")
    void createRule_missingRoleId_shouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "resourceType": "ORDER",
                    "ruleType": "ROW_LEVEL",
                    "conditionField": "dept",
                    "conditionOperator": "EQUALS",
                    "conditionValue": "IT"
                }
                """;

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Validation: POST /rules with conflicting allowed and denied fields returns 400")
    void createRule_conflictingFields_shouldReturn400() throws Exception {
        CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                roleId,
                "ORDER",
                RuleType.FIELD_LEVEL,
                null,
                null,
                null,
                new String[]{"creditCard", "cvv"},
                new String[]{"CREDITCARD"},
                1
        );

        mockMvc.perform(post("/api/v1/data-permissions/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
