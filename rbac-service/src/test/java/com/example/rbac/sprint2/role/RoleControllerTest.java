package com.example.qa.sprint2.role;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.rbac.controller.RoleController;
import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.model.Role;
import com.example.rbac.service.RoleServiceImpl;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleServiceImpl roleService;


    // ============================================================
    // SEARCH
    // ============================================================

    @Test
    void searchRoles_shouldReturnRoles() throws Exception {

        RoleResponseDto role = new RoleResponseDto();

        when(roleService.searchRoles(
                anyString(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt()
        )).thenReturn(List.of(role));

        mockMvc.perform(get("/api/v1/roles/search")
                        .param("query", "HR"))
                .andExpect(status().isOk());

        verify(roleService).searchRoles(
                eq("HR"),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt()
        );
    }


    @Test
    void searchRoles_shouldAllowOptionalParameters() throws Exception {

        when(roleService.searchRoles(
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt()
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles/search"))
                .andExpect(status().isOk());
    }


    @Test
    void searchRoles_shouldSupportQueryOnly() throws Exception {

        when(roleService.searchRoles(
                anyString(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt()
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles/search")
                        .param("query", "Admin"))
                .andExpect(status().isOk());
    }


    @Test
    void searchRoles_shouldSupportStatusOnly() throws Exception {

        when(roleService.searchRoles(
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt()
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles/search")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());
    }


    @Test
    void searchRoles_shouldSupportRoleTypeOnly() throws Exception {

        when(roleService.searchRoles(
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt()
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles/search")
                        .param("roleType", "CUSTOM"))
                .andExpect(status().isOk());
    }


    // ============================================================
    // STATUS
    // ============================================================

    @Test
    void updateStatus_shouldReturnUpdatedRole() throws Exception {

        RoleResponseDto response = new RoleResponseDto();

        when(roleService.updateStatus(
                eq(1L),
                anyString()
        )).thenReturn(response);

        mockMvc.perform(patch("/api/v1/roles/1/status")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(roleService).updateStatus(
                eq(1L),
                eq("ACTIVE")
        );
    }


    // ============================================================
    // COUNTS
    // ============================================================

    @Test
    void getRoleCounts_shouldReturnCounts() throws Exception {

        when(roleService.getRoleCounts())
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/roles/counts"))
                .andExpect(status().isOk());

        verify(roleService).getRoleCounts();
    }


    // ============================================================
    // CREATE
    // ============================================================

    @Test
    void createRole_shouldReturnCreatedRole() throws Exception {

        RoleRequestDto request = new RoleRequestDto();

        RoleResponseDto response = new RoleResponseDto();

        when(roleService.create(any(RoleRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType("application/json")
                        .content("""
                                {
                                  "roleName": "HR Manager",
                                  "roleType": "CUSTOM",
                                  "description": "HR management role"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(roleService).create(any(RoleRequestDto.class));
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    void getRoleById_shouldReturnRole() throws Exception {

        RoleResponseDto response = new RoleResponseDto();

        when(roleService.getById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/roles/1"))
                .andExpect(status().isOk());

        verify(roleService).getById(1L);
    }


    // ============================================================
    // GET ALL
    // ============================================================

    @Test
    void getAllRoles_shouldReturnRoles() throws Exception {

        when(roleService.getAll())
                .thenReturn(List.of(new RoleResponseDto()));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk());

        verify(roleService).getAll();
    }


    // ============================================================
    // UPDATE
    // ============================================================

    @Test
    void updateRole_shouldReturnUpdatedRole() throws Exception {

        RoleResponseDto response = new RoleResponseDto();

        when(roleService.update(
                eq(1L),
                any(RoleRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/roles/1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "roleName": "Updated HR Manager",
                                  "roleType": "CUSTOM",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk());

        verify(roleService).update(
                eq(1L),
                any(RoleRequestDto.class)
        );
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Test
    void deleteRole_shouldReturnNoContent() throws Exception {

        doNothing()
                .when(roleService)
                .deleteById(1L);

        mockMvc.perform(delete("/api/v1/roles/1"))
                .andExpect(status().isNoContent());

        verify(roleService).deleteById(1L);
    }
}