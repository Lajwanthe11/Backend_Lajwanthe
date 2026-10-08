package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.dto.request.CustomRoleRequest;
import com.example.platformadmin.rbac.dto.response.CustomRoleResponse;
import com.example.platformadmin.rbac.enums.CustomRoleStatus;
import com.example.platformadmin.rbac.service.CustomRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomRoleService customRoleService;

    private CustomRoleResponse response;
    private CustomRoleRequest request;

    private final UUID roleId =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final UUID permissionId1 =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private final UUID permissionId2 =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private final UUID permissionId3 =
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() {

        request = new CustomRoleRequest();

        request.setRoleName("HR Manager");
        request.setRoleCode("HR_MANAGER");
        request.setDescription("Custom role for HR managers");

        request.setPermissionIds(List.of(
                permissionId1,
                permissionId2,
                permissionId3
        ));

        request.setPublishNotes("Initial draft");

        response = new CustomRoleResponse();

        response.setRoleId(roleId);
        response.setRoleName("HR Manager");
        response.setRoleCode("HR_MANAGER");
        response.setDescription("Custom role for HR managers");

        response.setStatus(CustomRoleStatus.DRAFT);

        response.setDraftVersion(1);
        response.setPublishedVersion(0);

        response.setPermissionIds(List.of(
                permissionId1,
                permissionId2,
                permissionId3
        ));

        response.setPermissionCount(3);
    }

    // =========================================================
    // CREATE
    // POST /api/v1/roles/custom
    // =========================================================

    @Test
    void create_shouldReturnCreated() throws Exception {

        when(customRoleService.create(any(CustomRoleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/roles/custom")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleId").value(roleId.toString()))
                .andExpect(jsonPath("$.roleName").value("HR Manager"))
                .andExpect(jsonPath("$.roleCode").value("HR_MANAGER"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.draftVersion").value(1))
                .andExpect(jsonPath("$.publishedVersion").value(0))
                .andExpect(jsonPath("$.permissionCount").value(3));

        verify(customRoleService)
                .create(any(CustomRoleRequest.class));
    }

    // =========================================================
    // GET ALL
    // GET /api/v1/roles/custom
    // =========================================================

    @Test
    void getAll_shouldReturnCustomRoles() throws Exception {

        when(customRoleService.getAll())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/roles/custom")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].roleId").value(roleId.toString()))
                .andExpect(jsonPath("$[0].roleName").value("HR Manager"))
                .andExpect(jsonPath("$[0].roleCode").value("HR_MANAGER"))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));

        verify(customRoleService).getAll();
    }

    @Test
    void getAll_shouldReturnEmptyList() throws Exception {

        when(customRoleService.getAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/roles/custom")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(customRoleService).getAll();
    }

    // =========================================================
    // LIMITS
    // GET /api/v1/roles/custom/limits
    // =========================================================

    @Test
    void limits_shouldReturnLimits() throws Exception {

        Map<String, Object> limits = Map.of(
                "tenantId", "11111111-1111-1111-1111-111111111111",
                "currentCustomRoles", 5,
                "limit", "LICENSE_INTEGRATION_PENDING"
        );

        when(customRoleService.getLimits())
                .thenReturn(limits);

        mockMvc.perform(
                        get("/api/v1/roles/custom/limits")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentCustomRoles").value(5))
                .andExpect(jsonPath("$.limit")
                        .value("LICENSE_INTEGRATION_PENDING"));

        verify(customRoleService).getLimits();
    }

    // =========================================================
    // UPDATE
    // PUT /api/v1/roles/custom/{roleId}
    // =========================================================

    @Test
    void update_shouldReturnUpdatedRole() throws Exception {

        CustomRoleResponse updatedResponse =
                new CustomRoleResponse();

        updatedResponse.setRoleId(roleId);
        updatedResponse.setRoleName("HR Manager");
        updatedResponse.setRoleCode("HR_MANAGER");
        updatedResponse.setDescription("Updated HR role");

        updatedResponse.setStatus(CustomRoleStatus.DRAFT);

        updatedResponse.setDraftVersion(2);
        updatedResponse.setPublishedVersion(0);

        updatedResponse.setPermissionIds(List.of(
                permissionId1,
                permissionId2
        ));

        updatedResponse.setPermissionCount(2);

        when(customRoleService.update(
                eq(roleId),
                any(CustomRoleRequest.class)
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                        put("/api/v1/roles/custom/" + roleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$.draftVersion")
                        .value(2))
                .andExpect(jsonPath("$.permissionCount")
                        .value(2));

        verify(customRoleService).update(
                eq(roleId),
                any(CustomRoleRequest.class)
        );
    }

    // =========================================================
    // PUBLISH
    // POST /api/v1/roles/custom/{roleId}/publish
    // =========================================================

    @Test
    void publish_shouldPublishRole() throws Exception {

        CustomRoleResponse publishedResponse =
                new CustomRoleResponse();

        publishedResponse.setRoleId(roleId);
        publishedResponse.setRoleName("HR Manager");
        publishedResponse.setRoleCode("HR_MANAGER");

        publishedResponse.setStatus(CustomRoleStatus.PUBLISHED);

        publishedResponse.setDraftVersion(1);
        publishedResponse.setPublishedVersion(1);

        publishedResponse.setPermissionIds(List.of(
                permissionId1,
                permissionId2,
                permissionId3
        ));

        publishedResponse.setPermissionCount(3);

        when(customRoleService.publish(
                eq(roleId),
                eq("Initial publication")
        )).thenReturn(publishedResponse);

        mockMvc.perform(
                        post("/api/v1/roles/custom/"
                                + roleId
                                + "/publish")
                                .param(
                                        "publishNotes",
                                        "Initial publication"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedVersion")
                        .value(1));

        verify(customRoleService).publish(
                eq(roleId),
                eq("Initial publication")
        );
    }

    @Test
    void publish_withoutNotes_shouldWork() throws Exception {

        when(customRoleService.publish(
                eq(roleId),
                isNull()
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/roles/custom/"
                                + roleId
                                + "/publish")
                )
                .andExpect(status().isOk());

        verify(customRoleService).publish(
                eq(roleId),
                isNull()
        );
    }

    // =========================================================
    // ARCHIVE
    // POST /api/v1/roles/custom/{roleId}/archive
    // =========================================================

    @Test
    void archive_shouldArchiveRole() throws Exception {

        CustomRoleResponse archivedResponse =
                new CustomRoleResponse();

        archivedResponse.setRoleId(roleId);
        archivedResponse.setRoleName("HR Manager");
        archivedResponse.setRoleCode("HR_MANAGER");

        archivedResponse.setStatus(CustomRoleStatus.ARCHIVED);

        archivedResponse.setDraftVersion(1);
        archivedResponse.setPublishedVersion(1);

        archivedResponse.setPermissionIds(List.of(
                permissionId1,
                permissionId2,
                permissionId3
        ));

        archivedResponse.setPermissionCount(3);

        when(customRoleService.archive(roleId))
                .thenReturn(archivedResponse);

        mockMvc.perform(
                        post("/api/v1/roles/custom/"
                                + roleId
                                + "/archive")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("ARCHIVED"));

        verify(customRoleService).archive(roleId);
    }

    // =========================================================
    // VERSIONS
    // GET /api/v1/roles/custom/{roleId}/versions
    // =========================================================

    @Test
    void versions_shouldReturnRoleVersions() throws Exception {

        CustomRoleResponse version2 =
                new CustomRoleResponse();

        version2.setRoleId(roleId);
        version2.setRoleName("HR Manager");
        version2.setRoleCode("HR_MANAGER");

        version2.setVersionNumber(2);
        version2.setStatus(CustomRoleStatus.DRAFT);

        version2.setPermissionIds(List.of(
                permissionId1,
                permissionId2,
                permissionId3
        ));

        version2.setPermissionCount(3);

        CustomRoleResponse version1 =
                new CustomRoleResponse();

        version1.setRoleId(roleId);
        version1.setRoleName("HR Manager");
        version1.setRoleCode("HR_MANAGER");

        version1.setVersionNumber(1);
        version1.setStatus(CustomRoleStatus.PUBLISHED);

        version1.setPermissionIds(List.of(
                permissionId1,
                permissionId2
        ));

        version1.setPermissionCount(2);

        when(customRoleService.getVersions(roleId))
                .thenReturn(List.of(version2, version1));

        mockMvc.perform(
                        get("/api/v1/roles/custom/"
                                + roleId
                                + "/versions")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roleId")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$[0].versionNumber")
                        .value(2))
                .andExpect(jsonPath("$[0].status")
                        .value("DRAFT"))
                .andExpect(jsonPath("$[1].roleId")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$[1].versionNumber")
                        .value(1))
                .andExpect(jsonPath("$[1].status")
                        .value("PUBLISHED"));

        verify(customRoleService).getVersions(roleId);
    }

    // =========================================================
    // REVERT
    // POST /api/v1/roles/custom/{roleId}/revert/{version}
    // =========================================================

    @Test
    void revert_shouldCreateNewDraft() throws Exception {

        CustomRoleResponse revertedResponse =
                new CustomRoleResponse();

        revertedResponse.setRoleId(roleId);
        revertedResponse.setRoleName("HR Manager");
        revertedResponse.setRoleCode("HR_MANAGER");

        revertedResponse.setStatus(CustomRoleStatus.DRAFT);

        revertedResponse.setDraftVersion(3);
        revertedResponse.setPublishedVersion(1);

        revertedResponse.setPermissionIds(List.of(
                permissionId1,
                permissionId2
        ));

        revertedResponse.setPermissionCount(2);

        when(customRoleService.revert(
                eq(roleId),
                eq(1)
        )).thenReturn(revertedResponse);

        mockMvc.perform(
                        post("/api/v1/roles/custom/"
                                + roleId
                                + "/revert/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("DRAFT"))
                .andExpect(jsonPath("$.draftVersion")
                        .value(3));

        verify(customRoleService).revert(roleId, 1);
    }

    // =========================================================
    // IMPACT
    // GET /api/v1/roles/custom/{roleId}/impact
    // =========================================================

    @Test
    void impact_shouldReturnImpactInformation() throws Exception {

        Map<String, Object> impact = Map.of(
                "roleId", roleId,
                "message",
                "Impact analysis integration is pending"
        );

        when(customRoleService.getImpact(roleId))
                .thenReturn(impact);

        mockMvc.perform(
                        get("/api/v1/roles/custom/"
                                + roleId
                                + "/impact")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Impact analysis integration is pending"
                        ));

        verify(customRoleService).getImpact(roleId);
    }
}