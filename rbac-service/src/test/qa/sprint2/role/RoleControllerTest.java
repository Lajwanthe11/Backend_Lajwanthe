package com.example.qa.sprint2.role;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import com.example.rbac.RbacApplication;
import org.springframework.test.context.ContextConfiguration;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.rbac.controller.RoleController;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.enums.RoleType;
import com.example.rbac.service.serviceImpl.RoleServiceImpl;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleServiceImpl roleService;

    // =========================================================
    // SEARCH
    // =========================================================

    @Test
    void searchRoles_shouldReturnRoles() throws Exception {

        RoleResponseDto response =
                new RoleResponseDto(
                        1L,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "HR role",
                        "ACTIVE",
                        false,
                        null,
                        "test-user",
                        null,
                        "test-user"
                );

        when(roleService.searchRoles(
                "HR",
                RoleType.CUSTOM,
                "ACTIVE"
        )).thenReturn(List.of(response));

        mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("query", "HR")
                        .param("roleType", "CUSTOM")
                        .param("status", "ACTIVE")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].roleName")
                .value("HR Manager"))
        .andExpect(jsonPath("$[0].roleCode")
                .value("HR_MANAGER"))
        .andExpect(jsonPath("$[0].roleType")
                .value("CUSTOM"))
        .andExpect(jsonPath("$[0].status")
                .value("ACTIVE"));

        verify(roleService).searchRoles(
                "HR",
                RoleType.CUSTOM,
                "ACTIVE"
        );
    }

    @Test
    void searchRoles_shouldAllowOptionalParameters() throws Exception {

        when(roleService.searchRoles(
                null,
                null,
                null
        )).thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/roles/search")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

        verify(roleService).searchRoles(
                null,
                null,
                null
        );
    }

    @Test
    void searchRoles_shouldSupportQueryOnly() throws Exception {

        when(roleService.searchRoles(
                "HR",
                null,
                null
        )).thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("query", "HR")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());

        verify(roleService).searchRoles(
                "HR",
                null,
                null
        );
    }

    @Test
    void searchRoles_shouldSupportStatusOnly() throws Exception {

        when(roleService.searchRoles(
                null,
                null,
                "ACTIVE"
        )).thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("status", "ACTIVE")
        )
        .andExpect(status().isOk());

        verify(roleService).searchRoles(
                null,
                null,
                "ACTIVE"
        );
    }

    @Test
    void searchRoles_shouldSupportRoleTypeOnly() throws Exception {

        when(roleService.searchRoles(
                null,
                RoleType.CUSTOM,
                null
        )).thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("roleType", "CUSTOM")
        )
        .andExpect(status().isOk());

        verify(roleService).searchRoles(
                null,
                RoleType.CUSTOM,
                null
        );
    }

    // =========================================================
    // STATUS
    // =========================================================

    @Test
    void updateStatus_shouldReturnUpdatedRole() throws Exception {

        RoleResponseDto response =
                new RoleResponseDto(
                        1L,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "HR role",
                        "INACTIVE",
                        false,
                        null,
                        "test-user",
                        null,
                        "test-user"
                );

        when(roleService.updateStatus(
                1L,
                "INACTIVE"
        )).thenReturn(response);

        mockMvc.perform(
                patch("/api/v1/roles/1/status")
                        .param("status", "INACTIVE")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.roleName")
                .value("HR Manager"))
        .andExpect(jsonPath("$.status")
                .value("INACTIVE"));

        verify(roleService)
                .updateStatus(1L, "INACTIVE");
    }

    // =========================================================
    // ROLE COUNTS
    // =========================================================

    @Test
    void getRoleCounts_shouldReturnCounts() throws Exception {

        when(roleService.getRoleCounts())
                .thenReturn(
                        Map.of(
                                "totalRoles", 10L,
                                "systemRoles", 3L,
                                "customRoles", 7L
                        )
                );

        mockMvc.perform(
                get("/api/v1/roles/counts")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalRoles")
                .value(10))
        .andExpect(jsonPath("$.systemRoles")
                .value(3))
        .andExpect(jsonPath("$.customRoles")
                .value(7));

        verify(roleService).getRoleCounts();
    }
}