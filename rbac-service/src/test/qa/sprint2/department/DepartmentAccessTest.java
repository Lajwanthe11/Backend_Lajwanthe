package com.example.qa.sprint2.department;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.rbac.controller.DepartmentPermissionController;
import com.example.rbac.service.DepartmentPermissionService;

@WebMvcTest(DepartmentPermissionController.class)
class DepartmentAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentPermissionService departmentPermissionService;

    // =========================================================
    // GET
    // =========================================================

    @Test
    void shouldGetDepartmentScope() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UUID department1 = UUID.randomUUID();
        UUID department2 = UUID.randomUUID();
        UUID department3 = UUID.randomUUID();

        when(departmentPermissionService.getDepartmentScope(roleId))
                .thenReturn(List.of(
                        department1,
                        department2,
                        department3
                ));

        mockMvc.perform(
                get("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0]").value(department1.toString()))
        .andExpect(jsonPath("$[1]").value(department2.toString()))
        .andExpect(jsonPath("$[2]").value(department3.toString()));

        verify(departmentPermissionService)
                .getDepartmentScope(roleId);
    }

    @Test
    void shouldReturnEmptyDepartmentScope() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(departmentPermissionService.getDepartmentScope(roleId))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

        verify(departmentPermissionService)
                .getDepartmentScope(roleId);
    }

    @Test
    void shouldUseRoleIdFromPathForGet() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UUID departmentId = UUID.randomUUID();

        when(departmentPermissionService.getDepartmentScope(roleId))
                .thenReturn(List.of(departmentId));

        mockMvc.perform(
                get("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value(departmentId.toString()));

        verify(departmentPermissionService)
                .getDepartmentScope(roleId);

        verify(departmentPermissionService, never())
                .getDepartmentScope(eq(userId));
    }

    // =========================================================
    // PUT
    // =========================================================

    @Test
    void shouldUpdateDepartmentScope() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UUID department1 = UUID.randomUUID();
        UUID department2 = UUID.randomUUID();

        doNothing()
                .when(departmentPermissionService)
                .updateDepartmentScope(
                        eq(roleId),
                        eq(List.of(department1, department2))
                );

        mockMvc.perform(
                put("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": ["%s", "%s"]
                    }
                    """.formatted(
                        department1,
                        department2
                ))
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        roleId,
                        List.of(department1, department2)
                );
    }

    @Test
    void shouldUpdateDepartmentScopeWithSingleDepartment()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        mockMvc.perform(
                put("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": ["%s"]
                    }
                    """.formatted(departmentId))
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        roleId,
                        List.of(departmentId)
                );
    }

    @Test
    void shouldPassEmptyDepartmentListToService()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(
                put("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": []
                    }
                    """)
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        roleId,
                        List.of()
                );
    }

    @Test
    void shouldUseRoleIdFromPathForUpdate()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UUID department1 = UUID.randomUUID();
        UUID department2 = UUID.randomUUID();

        mockMvc.perform(
                put("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": ["%s", "%s"]
                    }
                    """.formatted(
                        department1,
                        department2
                ))
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        roleId,
                        List.of(department1, department2)
                );

        verify(departmentPermissionService, never())
                .updateDepartmentScope(
                        eq(userId),
                        anyList()
                );
    }

    // =========================================================
    // MISSING FIELD
    // =========================================================

    @Test
    void shouldPassMissingDepartmentIdsAsNullToService()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(
                put("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {}
                    """)
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        roleId,
                        null
                );
    }

    // =========================================================
    // INVALID JSON
    // =========================================================

    @Test
    void shouldRejectMalformedJson() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(
                put("/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": [
                    """)
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(departmentPermissionService);
    }

    // =========================================================
    // INVALID UUID
    // =========================================================

    @Test
    void shouldRejectInvalidUuidInPath() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                get(
                        "/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        "not-a-valid-uuid"
                )
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(departmentPermissionService);
    }

    @Test
    void shouldRejectInvalidDepartmentUuid() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(
                put(
                        "/api/v1/users/{userId}/roles/{roleId}/departments",
                        userId,
                        roleId
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": ["not-a-valid-uuid"]
                    }
                    """)
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(departmentPermissionService);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldRemoveAllDepartmentScope()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(
                delete(
                        "/api/v1/users/{userId}/roles/{roleId}/departments/all",
                        userId,
                        roleId
                )
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .removeAllDepartmentScope(roleId);
    }

    @Test
    void shouldUseRoleIdFromPathForDelete()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(
                delete(
                        "/api/v1/users/{userId}/roles/{roleId}/departments/all",
                        userId,
                        roleId
                )
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .removeAllDepartmentScope(roleId);

        verify(departmentPermissionService, never())
                .removeAllDepartmentScope(userId);
    }
}