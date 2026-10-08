package com.example.platformadmin.rbac.service;

import com.example.common.security.user.JwtUserPrincipal;
import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.dto.request.CreateDataAccessRuleRequest;
import com.example.platformadmin.rbac.dto.request.RuleTestRequest;
import com.example.platformadmin.rbac.dto.request.UpdateDataAccessRuleRequest;
import com.example.platformadmin.rbac.dto.response.DataAccessRuleResponse;
import com.example.platformadmin.rbac.dto.response.DataPermissionReportResponse;
import com.example.platformadmin.rbac.dto.response.RuleTestResponse;
import com.example.platformadmin.rbac.entity.DataAccessRule;
import com.example.platformadmin.rbac.enums.ConditionOperator;
import com.example.platformadmin.rbac.enums.RuleType;
import com.example.platformadmin.rbac.exception.DataAccessRuleNotFoundException;
import com.example.platformadmin.rbac.exception.InvalidDataAccessRuleException;
import com.example.platformadmin.rbac.repository.DataAccessRuleRepository;
import com.example.platformadmin.rbac.service.serviceImpl.DataAccessRuleServiceImpl;
import com.example.platformadmin.rbac.service.serviceImpl.DataPermissionEvaluationService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataAccessRuleServiceImplTest {

        @Mock
        private DataAccessRuleRepository repository;

        @Mock
        private DataPermissionEvaluationService evaluationService;

        @InjectMocks
        private DataAccessRuleServiceImpl service;

        private final UUID tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        private final UUID roleId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        private final UUID ruleId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        private final UUID userId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        @BeforeEach
        void setUp() {
                TenantContext.setTenantId(tenantId.toString());
        }

        @AfterEach
        void tearDown() {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
        }

        private void mockAuthentication(UUID uid) {
                Authentication auth = mock(Authentication.class);
                JwtUserPrincipal principal = mock(JwtUserPrincipal.class);
                lenient().when(principal.getUsername()).thenReturn(uid.toString());
                lenient().when(auth.getPrincipal()).thenReturn(principal);
                SecurityContext context = mock(SecurityContext.class);
                lenient().when(context.getAuthentication()).thenReturn(auth);
                SecurityContextHolder.setContext(context);
        }

        // ============================================================
        // CREATE - Valid Scenarios
        // ============================================================

        @Test
        @DisplayName("create - should successfully create a ROW_LEVEL rule")
        void create_rowLevelRule_shouldSucceed() {
                mockAuthentication(userId);
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId,
                                "order",
                                RuleType.ROW_LEVEL,
                                "departmentId",
                                ConditionOperator.EQUALS,
                                "finance",
                                null,
                                null,
                                15);

                when(repository.save(any(DataAccessRule.class))).thenAnswer(invocation -> {
                        DataAccessRule rule = invocation.getArgument(0);
                        rule.setRuleId(ruleId);
                        return rule;
                });

                DataAccessRuleResponse response = service.create(request);

                assertThat(response).isNotNull();
                assertThat(response.ruleId()).isEqualTo(ruleId);
                assertThat(response.tenantId()).isEqualTo(tenantId);
                assertThat(response.roleId()).isEqualTo(roleId);
                assertThat(response.resourceType()).isEqualTo("ORDER");
                assertThat(response.ruleType()).isEqualTo(RuleType.ROW_LEVEL);
                assertThat(response.conditionField()).isEqualTo("departmentId");
                assertThat(response.conditionOperator()).isEqualTo(ConditionOperator.EQUALS);
                assertThat(response.conditionValue()).isEqualTo("finance");
                assertThat(response.priority()).isEqualTo(15);
                assertThat(response.active()).isTrue();
                assertThat(response.createdBy()).isEqualTo(userId);

                ArgumentCaptor<DataAccessRule> captor = ArgumentCaptor.forClass(DataAccessRule.class);
                verify(repository).save(captor.capture());
                DataAccessRule saved = captor.getValue();
                assertThat(saved.getResourceType()).isEqualTo("ORDER");
                assertThat(saved.getPriority()).isEqualTo(15);
        }

        @Test
        @DisplayName("create - should successfully create a FIELD_LEVEL rule with allowed and denied fields")
        void create_fieldLevelRule_shouldSucceed() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId,
                                "EMPLOYEE",
                                RuleType.FIELD_LEVEL,
                                null,
                                null,
                                null,
                                new String[] { "id", "name", "email" },
                                new String[] { "salary", "ssn" },
                                null // null priority should default to 0
                );

                when(repository.save(any(DataAccessRule.class))).thenAnswer(invocation -> {
                        DataAccessRule rule = invocation.getArgument(0);
                        rule.setRuleId(ruleId);
                        return rule;
                });

                DataAccessRuleResponse response = service.create(request);

                assertThat(response).isNotNull();
                assertThat(response.ruleType()).isEqualTo(RuleType.FIELD_LEVEL);
                assertThat(response.allowedFields()).containsExactly("id", "name", "email");
                assertThat(response.deniedFields()).containsExactly("salary", "ssn");
                assertThat(response.priority()).isEqualTo(0);
                // Fallback user ID when security context has no auth
                assertThat(response.createdBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }

        @Test
        @DisplayName("create - should succeed with allowedFields only for FIELD_LEVEL")
        void create_fieldLevelRule_allowedFieldsOnly_shouldSucceed() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId,
                                "CUSTOMER",
                                RuleType.FIELD_LEVEL,
                                null,
                                null,
                                null,
                                new String[] { "id", "name" },
                                null,
                                5);

                when(repository.save(any(DataAccessRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

                DataAccessRuleResponse response = service.create(request);
                assertThat(response.allowedFields()).containsExactly("id", "name");
                assertThat(response.deniedFields()).isNull();
        }

        @Test
        @DisplayName("create - should succeed with deniedFields only for FIELD_LEVEL")
        void create_fieldLevelRule_deniedFieldsOnly_shouldSucceed() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId,
                                "CUSTOMER",
                                RuleType.FIELD_LEVEL,
                                null,
                                null,
                                null,
                                null,
                                new String[] { "creditCardNumber" },
                                5);

                when(repository.save(any(DataAccessRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

                DataAccessRuleResponse response = service.create(request);
                assertThat(response.deniedFields()).containsExactly("creditCardNumber");
                assertThat(response.allowedFields()).isNull();
        }

        // ============================================================
        // CREATE - Validation Errors
        // ============================================================

        @Test
        @DisplayName("create - should throw InvalidDataAccessRuleException when roleId is null")
        void create_nullRoleId_throwsException() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                null, "ORDER", RuleType.ROW_LEVEL, "dept", ConditionOperator.EQUALS, "IT", null, null,
                                0);

                assertThatThrownBy(() -> service.create(request))
                                .isInstanceOf(InvalidDataAccessRuleException.class)
                                .hasMessageContaining("Role ID is required");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("create - should throw InvalidDataAccessRuleException when resourceType is blank")
        void create_blankResourceType_throwsException() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId, "   ", RuleType.ROW_LEVEL, "dept", ConditionOperator.EQUALS, "IT", null, null,
                                0);

                assertThatThrownBy(() -> service.create(request))
                                .isInstanceOf(InvalidDataAccessRuleException.class)
                                .hasMessageContaining("Resource type is required");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("create - should throw InvalidDataAccessRuleException when ruleType is null")
        void create_nullRuleType_throwsException() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId, "ORDER", null, "dept", ConditionOperator.EQUALS, "IT", null, null, 0);

                assertThatThrownBy(() -> service.create(request))
                                .isInstanceOf(InvalidDataAccessRuleException.class)
                                .hasMessageContaining("Rule type is required");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("create - should throw InvalidDataAccessRuleException when ROW_LEVEL has no conditionOperator")
        void create_rowLevelMissingConditionOperator_throwsException() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId, "ORDER", RuleType.ROW_LEVEL, "dept", null, "IT", null, null, 0);

                assertThatThrownBy(() -> service.create(request))
                                .isInstanceOf(InvalidDataAccessRuleException.class)
                                .hasMessageContaining("Condition operator is required for ROW_LEVEL rule");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("create - should throw InvalidDataAccessRuleException when FIELD_LEVEL has no fields")
        void create_fieldLevelMissingBothAllowedAndDenied_throwsException() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId, "ORDER", RuleType.FIELD_LEVEL, null, null, null, new String[0], null, 0);

                assertThatThrownBy(() -> service.create(request))
                                .isInstanceOf(InvalidDataAccessRuleException.class)
                                .hasMessageContaining("Allowed or denied fields must be provided for FIELD_LEVEL rule");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("create - should throw InvalidDataAccessRuleException when a field is both allowed and denied")
        void create_conflictingAllowedAndDeniedFields_throwsException() {
                CreateDataAccessRuleRequest request = new CreateDataAccessRuleRequest(
                                roleId,
                                "ORDER",
                                RuleType.FIELD_LEVEL,
                                null,
                                null,
                                null,
                                new String[] { "totalAmount", "customerName" },
                                new String[] { "TOTALAMOUNT", "tax" }, // case-insensitive overlap
                                0);

                assertThatThrownBy(() -> service.create(request))
                                .isInstanceOf(InvalidDataAccessRuleException.class)
                                .hasMessageContaining("A field cannot be both allowed and denied: totalAmount");

                verify(repository, never()).save(any());
        }

        // ============================================================
        // UPDATE
        // ============================================================

        @Test
        @DisplayName("update - should update existing rule fields successfully")
        void update_existingRule_shouldSucceed() {
                DataAccessRule existingRule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(tenantId)
                                .roleId(roleId)
                                .resourceType("ORDER")
                                .ruleType(RuleType.ROW_LEVEL)
                                .conditionField("departmentId")
                                .conditionOperator(ConditionOperator.EQUALS)
                                .conditionValue("FINANCE")
                                .priority(5)
                                .active(true)
                                .createdBy(userId)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(repository.findById(ruleId)).thenReturn(Optional.of(existingRule));
                when(repository.save(any(DataAccessRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

                UUID newRoleId = UUID.randomUUID();
                UpdateDataAccessRuleRequest updateRequest = new UpdateDataAccessRuleRequest(
                                newRoleId,
                                "invoice",
                                RuleType.FIELD_LEVEL,
                                "status",
                                ConditionOperator.IN,
                                "PAID",
                                new String[] { "id", "amount" },
                                new String[] { "secretNotes" },
                                25,
                                false);

                DataAccessRuleResponse response = service.update(ruleId, updateRequest);

                assertThat(response).isNotNull();
                assertThat(response.roleId()).isEqualTo(newRoleId);
                assertThat(response.resourceType()).isEqualTo("INVOICE");
                assertThat(response.ruleType()).isEqualTo(RuleType.FIELD_LEVEL);
                assertThat(response.conditionField()).isEqualTo("status");
                assertThat(response.conditionOperator()).isEqualTo(ConditionOperator.IN);
                assertThat(response.conditionValue()).isEqualTo("PAID");
                assertThat(response.allowedFields()).containsExactly("id", "amount");
                assertThat(response.deniedFields()).containsExactly("secretNotes");
                assertThat(response.priority()).isEqualTo(25);
                assertThat(response.active()).isFalse();

                verify(repository).save(existingRule);
        }

        @Test
        @DisplayName("update - should throw DataAccessRuleNotFoundException when rule does not exist")
        void update_nonExistentRule_throwsException() {
                when(repository.findById(ruleId)).thenReturn(Optional.empty());

                UpdateDataAccessRuleRequest updateRequest = new UpdateDataAccessRuleRequest(
                                roleId, "ORDER", null, null, null, null, null, null, null, null);

                assertThatThrownBy(() -> service.update(ruleId, updateRequest))
                                .isInstanceOf(DataAccessRuleNotFoundException.class)
                                .hasMessageContaining("Data access rule not found: " + ruleId);

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("update - should throw DataAccessRuleNotFoundException when rule belongs to another tenant")
        void update_crossTenantRule_throwsException() {
                UUID anotherTenantId = UUID.randomUUID();
                DataAccessRule existingRule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(anotherTenantId)
                                .roleId(roleId)
                                .resourceType("ORDER")
                                .ruleType(RuleType.ROW_LEVEL)
                                .build();

                when(repository.findById(ruleId)).thenReturn(Optional.of(existingRule));

                UpdateDataAccessRuleRequest updateRequest = new UpdateDataAccessRuleRequest(
                                roleId, "ORDER", null, null, null, null, null, null, null, null);

                assertThatThrownBy(() -> service.update(ruleId, updateRequest))
                                .isInstanceOf(DataAccessRuleNotFoundException.class)
                                .hasMessageContaining("Data access rule not found: " + ruleId);

                verify(repository, never()).save(any());
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @Test
        @DisplayName("getById - should return rule response when found and matches tenant")
        void getById_existingRule_shouldReturnResponse() {
                DataAccessRule rule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(tenantId)
                                .roleId(roleId)
                                .resourceType("ORDER")
                                .ruleType(RuleType.ROW_LEVEL)
                                .active(true)
                                .priority(1)
                                .createdBy(userId)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(repository.findById(ruleId)).thenReturn(Optional.of(rule));

                DataAccessRuleResponse response = service.getById(ruleId);

                assertThat(response).isNotNull();
                assertThat(response.ruleId()).isEqualTo(ruleId);
                assertThat(response.tenantId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("getById - should throw DataAccessRuleNotFoundException when not found")
        void getById_notFound_throwsException() {
                when(repository.findById(ruleId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.getById(ruleId))
                                .isInstanceOf(DataAccessRuleNotFoundException.class)
                                .hasMessageContaining("Data access rule not found: " + ruleId);
        }

        @Test
        @DisplayName("getById - should throw DataAccessRuleNotFoundException when tenant does not match")
        void getById_differentTenant_throwsException() {
                DataAccessRule rule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(UUID.randomUUID())
                                .build();

                when(repository.findById(ruleId)).thenReturn(Optional.of(rule));

                assertThatThrownBy(() -> service.getById(ruleId))
                                .isInstanceOf(DataAccessRuleNotFoundException.class);
        }

        // ============================================================
        // GET RULES
        // ============================================================

        @Test
        @DisplayName("getRules - should return list of active rules for tenant")
        void getRules_shouldReturnRulesList() {
                DataAccessRule rule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(tenantId)
                                .roleId(roleId)
                                .resourceType("ORDER")
                                .ruleType(RuleType.ROW_LEVEL)
                                .active(true)
                                .priority(10)
                                .createdBy(userId)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(repository.findByTenantIdAndActiveTrue(tenantId)).thenReturn(List.of(rule));

                List<DataAccessRuleResponse> rules = service.getRules();

                assertThat(rules).hasSize(1);
                assertThat(rules.get(0).ruleId()).isEqualTo(ruleId);
        }

        @Test
        @DisplayName("getRules - should return empty list when no active rules exist")
        void getRules_whenEmpty_shouldReturnEmptyList() {
                when(repository.findByTenantIdAndActiveTrue(tenantId)).thenReturn(Collections.emptyList());

                List<DataAccessRuleResponse> rules = service.getRules();

                assertThat(rules).isEmpty();
        }

        @Test
        @DisplayName("getRules - should return empty list when repository returns null")
        void getRules_whenNull_shouldReturnEmptyList() {
                when(repository.findByTenantIdAndActiveTrue(tenantId)).thenReturn(null);

                List<DataAccessRuleResponse> rules = service.getRules();

                assertThat(rules).isEmpty();
        }

        // ============================================================
        // GET RULES BY ROLE
        // ============================================================

        @Test
        @DisplayName("getRulesByRole - should return rules for role")
        void getRulesByRole_shouldReturnRulesList() {
                DataAccessRule rule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(tenantId)
                                .roleId(roleId)
                                .resourceType("ORDER")
                                .ruleType(RuleType.ROW_LEVEL)
                                .active(true)
                                .priority(10)
                                .createdBy(userId)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(repository.findByTenantIdAndRoleIdAndActiveTrue(tenantId, roleId)).thenReturn(List.of(rule));

                List<DataAccessRuleResponse> rules = service.getRulesByRole(roleId);

                assertThat(rules).hasSize(1);
                assertThat(rules.get(0).roleId()).isEqualTo(roleId);
        }

        @Test
        @DisplayName("getRulesByRole - should return empty list when none exist")
        void getRulesByRole_whenEmpty_shouldReturnEmptyList() {
                when(repository.findByTenantIdAndRoleIdAndActiveTrue(tenantId, roleId))
                                .thenReturn(Collections.emptyList());

                List<DataAccessRuleResponse> rules = service.getRulesByRole(roleId);

                assertThat(rules).isEmpty();
        }

        // ============================================================
        // DELETE
        // ============================================================

        @Test
        @DisplayName("delete - should delete rule when found and matches tenant")
        void delete_existingRule_shouldSucceed() {
                DataAccessRule rule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(tenantId)
                                .build();

                when(repository.findById(ruleId)).thenReturn(Optional.of(rule));

                service.delete(ruleId);

                verify(repository).delete(rule);
        }

        @Test
        @DisplayName("delete - should throw DataAccessRuleNotFoundException when not found")
        void delete_nonExistentRule_throwsException() {
                when(repository.findById(ruleId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.delete(ruleId))
                                .isInstanceOf(DataAccessRuleNotFoundException.class);

                verify(repository, never()).delete(any());
        }

        @Test
        @DisplayName("delete - should throw DataAccessRuleNotFoundException when tenant does not match")
        void delete_differentTenant_throwsException() {
                DataAccessRule rule = DataAccessRule.builder()
                                .ruleId(ruleId)
                                .tenantId(UUID.randomUUID())
                                .build();

                when(repository.findById(ruleId)).thenReturn(Optional.of(rule));

                assertThatThrownBy(() -> service.delete(ruleId))
                                .isInstanceOf(DataAccessRuleNotFoundException.class);

                verify(repository, never()).delete(any());
        }

        // ============================================================
        // TEST RULE SIMULATION
        // ============================================================

        @Test
        @DisplayName("testRule - should delegate to evaluationService")
        void testRule_shouldDelegateToEvaluationService() {
                RuleTestRequest request = new RuleTestRequest(
                                userId,
                                roleId,
                                "ORDER",
                                Map.of("id", 1),
                                Map.of());

                RuleTestResponse expectedResponse = new RuleTestResponse(
                                true,
                                null,
                                new String[] { "id" },
                                new String[0],
                                Map.of("id", 1),
                                Collections.emptyList());

                when(evaluationService.simulateRuleTest(request)).thenReturn(expectedResponse);

                RuleTestResponse response = service.testRule(request);

                assertThat(response).isEqualTo(expectedResponse);
                verify(evaluationService).simulateRuleTest(request);
        }

        // ============================================================
        // GET REPORT
        // ============================================================

        @Test
        @DisplayName("getReport - should group rules by role and resource type and compute counts")
        void getReport_shouldGroupRulesAndReturnReports() {
                DataAccessRule rowRule = DataAccessRule.builder()
                                .ruleId(UUID.randomUUID())
                                .tenantId(tenantId)
                                .roleId(roleId)
                                .resourceType("ORDER")
                                .ruleType(RuleType.ROW_LEVEL)
                                .active(true)
                                .priority(1)
                                .createdBy(userId)
                                .createdAt(LocalDateTime.now())
                                .build();

                DataAccessRule fieldRule = DataAccessRule.builder()
                                .ruleId(UUID.randomUUID())
                                .tenantId(tenantId)
                                .roleId(roleId)
                                .resourceType("ORDER")
                                .ruleType(RuleType.FIELD_LEVEL)
                                .active(true)
                                .priority(2)
                                .createdBy(userId)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(repository.findByTenantId(tenantId)).thenReturn(List.of(rowRule, fieldRule));

                List<DataPermissionReportResponse> report = service.getReport();

                assertThat(report).hasSize(1);
                DataPermissionReportResponse item = report.get(0);
                assertThat(item.roleId()).isEqualTo(roleId);
                assertThat(item.resourceType()).isEqualTo("ORDER");
                assertThat(item.totalRules()).isEqualTo(2);
                assertThat(item.rowLevelRulesCount()).isEqualTo(1);
                assertThat(item.fieldLevelRulesCount()).isEqualTo(1);
                assertThat(item.rules()).hasSize(2);
        }

        @Test
        @DisplayName("getReport - should return empty list when no rules exist for tenant")
        void getReport_whenNoRulesExist_shouldReturnEmptyList() {
                when(repository.findByTenantId(tenantId)).thenReturn(Collections.emptyList());

                List<DataPermissionReportResponse> report = service.getReport();

                assertThat(report).isEmpty();
        }
}
