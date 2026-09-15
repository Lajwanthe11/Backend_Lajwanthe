package com.example.rbac.controller;

import com.example.rbac.dto.DepartmentRoleDistribution;
import com.example.rbac.dto.ExpiryNotificationResponse;
import com.example.rbac.dto.RoleAssignmentReportRow;
import com.example.rbac.exception.GlobalExceptionHandler;
import com.example.rbac.service.RoleAssignmentExportService;
import com.example.rbac.service.RoleAssignmentQueryService;
import com.example.rbac.service.RoleExpiryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoleAssignmentReportControllerTest {

    @Mock
    private RoleAssignmentQueryService queryService;
    @Mock
    private RoleAssignmentExportService exportService;
    @Mock
    private RoleExpiryService expiryService;

    private MockMvc mockMvc;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoleAssignmentReportController controller = new RoleAssignmentReportController(
                queryService, exportService, expiryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        tenantId = UUID.randomUUID();
    }

    @Test
    void reportJson_shouldReturnRows() throws Exception {
        RoleAssignmentReportRow row = new RoleAssignmentReportRow(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "HR_MANAGER", "HR Manager",
                LocalDate.now(), LocalDate.now().plusDays(30),
                "ACTIVE", false, LocalDateTime.now());
        when(queryService.getReport(tenantId)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/roles/assignments/report")
                        .header("X-Tenant-Id", tenantId)
                        .param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleCode").value("HR_MANAGER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void reportExcel_shouldReturnAttachment() throws Exception {
        when(queryService.getReport(tenantId)).thenReturn(List.of());
        when(exportService.toExcel(anyList())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/v1/roles/assignments/report")
                        .header("X-Tenant-Id", tenantId)
                        .param("format", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition", "attachment; filename=role-assignment-report.xlsx"))
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void reportPdf_shouldReturnAttachment() throws Exception {
        when(queryService.getReport(tenantId)).thenReturn(List.of());
        when(exportService.toPdf(anyList())).thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/api/v1/roles/assignments/report")
                        .header("X-Tenant-Id", tenantId)
                        .param("format", "pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void report_shouldReturn400ForUnsupportedFormat() throws Exception {
        when(queryService.getReport(tenantId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles/assignments/report")
                        .header("X-Tenant-Id", tenantId)
                        .param("format", "txt"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("format must be one of json, xlsx, excel, or pdf"));
    }

    @Test
    void byDepartment_shouldReturnDistribution() throws Exception {
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        when(queryService.getDistributionByDepartment(tenantId))
                .thenReturn(List.of(new DepartmentRoleDistribution(
                        departmentId, roleId, "HR_MANAGER", "HR Manager", 3)));

        mockMvc.perform(get("/api/v1/roles/assignments/by-department")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userCount").value(3));
    }

    @Test
    void expiring_shouldPassFiltersToService() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        when(queryService.getExpiring(
                eq(tenantId), eq(7), eq(organizationId), eq(departmentId), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles/assignments/expiring")
                        .header("X-Tenant-Id", tenantId)
                        .param("days", "7")
                        .param("organizationId", organizationId.toString())
                        .param("departmentId", departmentId.toString())
                        .param("roleType", "SYSTEM"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void expiryNotify_shouldReturnServiceResult() throws Exception {
        LocalDate target = LocalDate.now().plusDays(7);
        when(expiryService.triggerForTenant(tenantId))
                .thenReturn(new ExpiryNotificationResponse(target, 2, 2));

        mockMvc.perform(post("/api/v1/roles/assignments/expiry-notify")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchedAssignments").value(2))
                .andExpect(jsonPath("$.notificationsTriggered").value(2));
    }
}
