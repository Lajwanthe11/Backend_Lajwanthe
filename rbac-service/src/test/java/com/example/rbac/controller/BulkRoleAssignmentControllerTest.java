package com.example.rbac.controller;

import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import com.example.rbac.dto.BulkOperationItemResult;
import com.example.rbac.dto.BulkOperationResponse;
import com.example.rbac.dto.CsvImportResponse;
import com.example.rbac.exception.GlobalExceptionHandler;
import com.example.rbac.service.BulkRoleAssignmentService;
import com.example.rbac.service.CsvRoleImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BulkRoleAssignmentControllerTest {

    @Mock
    private BulkRoleAssignmentService bulkRoleAssignmentService;

    @Mock
    private CsvRoleImportService csvRoleImportService;

    @Mock
    private SecurityContextUtil securityContextUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID tenantId;
    private UUID actorId;
    private UUID roleId;
    private UUID userId;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        userId = UUID.randomUUID();

        /*
         * Controller no longer reads tenant/user from request headers.
         * It obtains them from SecurityContextUtil.
         */
        when(securityContextUtil.currentUser())
                .thenReturn(
                        new AuthenticatedUser(
                                actorId.toString(),
                                tenantId.toString()
                        )
                );

        BulkRoleAssignmentController controller =
                new BulkRoleAssignmentController(
                        bulkRoleAssignmentService,
                        csvRoleImportService,
                        securityContextUtil
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void bulkAssign_shouldReturn200AndServiceResponse()
            throws Exception {

        BulkOperationResponse response =
                new BulkOperationResponse(
                        1,
                        1,
                        0,
                        0,
                        List.of(
                                new BulkOperationItemResult(
                                        userId,
                                        "ASSIGNED",
                                        "Role assigned"
                                )
                        )
                );

        when(
                bulkRoleAssignmentService.bulkAssign(
                        eq(tenantId),
                        eq(actorId),
                        any()
                )
        ).thenReturn(response);

        String body = """
                {
                  "userIds": ["%s"],
                  "roleId": "%s",
                  "effectiveDate": "%s",
                  "expiryDate": "%s",
                  "reason": "JUnit test"
                }
                """.formatted(
                userId,
                roleId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30)
        );

        mockMvc.perform(
                        post("/api/v1/users/roles/bulk-assign")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.requestedCount")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.successCount")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.results[0].status")
                                .value("ASSIGNED")
                );

        verify(securityContextUtil).currentUser();

        verify(bulkRoleAssignmentService)
                .bulkAssign(
                        eq(tenantId),
                        eq(actorId),
                        any()
                );
    }

    @Test
    void bulkAssign_shouldUseAuthenticatedUserFromSecurityContext()
            throws Exception {

        BulkOperationResponse response =
                new BulkOperationResponse(
                        1,
                        1,
                        0,
                        0,
                        List.of(
                                new BulkOperationItemResult(
                                        userId,
                                        "ASSIGNED",
                                        "Role assigned"
                                )
                        )
                );

        when(
                bulkRoleAssignmentService.bulkAssign(
                        eq(tenantId),
                        eq(actorId),
                        any()
                )
        ).thenReturn(response);

        String body = """
                {
                  "userIds": ["%s"],
                  "roleId": "%s",
                  "effectiveDate": "%s"
                }
                """.formatted(
                userId,
                roleId,
                LocalDate.now().plusDays(1)
        );

        /*
         * Notice:
         * No X-Tenant-Id header
         * No X-User-Id header
         */
        mockMvc.perform(
                        post("/api/v1/users/roles/bulk-assign")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk());

        verify(securityContextUtil).currentUser();

        verify(bulkRoleAssignmentService)
                .bulkAssign(
                        eq(tenantId),
                        eq(actorId),
                        any()
                );
    }

    @Test
    void bulkAssign_shouldReturn400ForEmptyUserIds()
            throws Exception {

        String body = """
                {
                  "userIds": [],
                  "roleId": "%s",
                  "effectiveDate": "%s"
                }
                """.formatted(
                roleId,
                LocalDate.now().plusDays(1)
        );

        mockMvc.perform(
                        post("/api/v1/users/roles/bulk-assign")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Request validation failed")
                );

        /*
         * Validation fails before the controller method executes,
         * therefore neither SecurityContextUtil nor service is called.
         */
        verifyNoInteractions(bulkRoleAssignmentService);
    }

    @Test
    void bulkRevoke_shouldReturn200()
            throws Exception {

        BulkOperationResponse response =
                new BulkOperationResponse(
                        1,
                        1,
                        0,
                        0,
                        List.of(
                                new BulkOperationItemResult(
                                        userId,
                                        "REVOKED",
                                        "Role revoked successfully"
                                )
                        )
                );

        when(
                bulkRoleAssignmentService.bulkRevoke(
                        eq(tenantId),
                        eq(actorId),
                        any()
                )
        ).thenReturn(response);

        String body = """
                {
                  "userIds": ["%s"],
                  "roleId": "%s",
                  "reason": "Project ended"
                }
                """.formatted(
                userId,
                roleId
        );

        mockMvc.perform(
                        post("/api/v1/users/roles/bulk-revoke")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.results[0].status")
                                .value("REVOKED")
                );

        verify(securityContextUtil).currentUser();

        verify(bulkRoleAssignmentService)
                .bulkRevoke(
                        eq(tenantId),
                        eq(actorId),
                        any()
                );
    }

    @Test
    void bulkRevoke_shouldReturn400WhenReasonIsBlank()
            throws Exception {

        String body = """
                {
                  "userIds": ["%s"],
                  "roleId": "%s",
                  "reason": ""
                }
                """.formatted(
                userId,
                roleId
        );

        mockMvc.perform(
                        post("/api/v1/users/roles/bulk-revoke")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.fieldErrors.reason")
                                .value(
                                        "reason is mandatory for bulk revoke"
                                )
                );

        verifyNoInteractions(bulkRoleAssignmentService);
    }

    @Test
    void csvImport_shouldAcceptMultipartFile()
            throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "roles.csv",
                        "text/csv",
                        """
                        employeeId,roleCode,effectiveDate,expiryDate
                        """.getBytes()
                );

        when(
                csvRoleImportService.importCsv(
                        eq(tenantId),
                        eq(actorId),
                        any()
                )
        ).thenReturn(
                new CsvImportResponse(
                        0,
                        0,
                        0,
                        0,
                        List.of()
                )
        );

        mockMvc.perform(
                        multipart("/api/v1/users/roles/import")
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalRows")
                                .value(0)
                );

        verify(securityContextUtil).currentUser();

        verify(csvRoleImportService)
                .importCsv(
                        eq(tenantId),
                        eq(actorId),
                        any()
                );
    }
}