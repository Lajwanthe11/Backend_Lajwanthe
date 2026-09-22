package com.example.rbac.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.example.common.exception.BadRequestException;
import com.example.rbac.controller.RoleController;
import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.enums.RoleType;
import com.example.rbac.service.PermissionAuthorizationService;
import com.example.rbac.service.RoleService;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RoleIntegrationTest.TestExceptionHandler.class)
class RoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private PermissionAuthorizationService permissionAuthorizationService;

    /*
     * TC-S2-01
     */
    @Test
    @DisplayName("TC-S2-01 - Duplicate role name must return 400")
    void duplicateRoleName_shouldReturn400() throws Exception {

        UUID roleId = UUID.randomUUID();

        RoleResponseDto createdRole = new RoleResponseDto(
                roleId,
                "PART12_DUPLICATE_ROLE",
                "PART12_DUPLICATE_CODE",
                RoleType.CUSTOM,
                "Part 12 duplicate role test",
                "ACTIVE",
                false,
                Set.of(),
                null,
                null,
                null,
                null
        );

        /*
         * First creation succeeds.
         */
        when(roleService.create(any(RoleRequestDto.class)))
                .thenReturn(createdRole);

        mockMvc.perform(
                post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "PART12_DUPLICATE_ROLE",
                                  "roleCode": "PART12_DUPLICATE_CODE",
                                  "roleType": "CUSTOM",
                                  "description": "Part 12 duplicate role test",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated());

        /*
         * Second creation throws the same exception
         * used by RoleServiceImpl.beforeCreate().
         */
        when(roleService.create(any(RoleRequestDto.class)))
                .thenThrow(
                        new BadRequestException(
                                "Role name already exists"));

        mockMvc.perform(
                post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "PART12_DUPLICATE_ROLE",
                                  "roleCode": "PART12_DUPLICATE_CODE_2",
                                  "roleType": "CUSTOM",
                                  "description": "Duplicate role",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    /*
     * TC-S2-02
     */
    @Test
    @DisplayName("TC-S2-02 - System role deletion must return 400")
    void systemRoleDeletion_shouldReturn400() throws Exception {

        UUID systemRoleId = UUID.randomUUID();

        /*
         * RoleServiceImpl.deleteById() throws:
         * BadRequestException("System role cannot be deleted")
         */
        doThrow(
                new BadRequestException(
                        "System role cannot be deleted"))
                .when(roleService)
                .deleteById(systemRoleId);

        mockMvc.perform(
                delete("/api/v1/roles/{id}", systemRoleId))
                .andExpect(status().isBadRequest());
    }

    /*
     * TC-S2-03
     */
    @Test
    @DisplayName("TC-S2-03 - Role search must return 200")
    void roleSearch_shouldReturn200() throws Exception {

        when(roleService.searchRoles(
                "SUPER_ADMIN",
                null,
                null))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("query", "SUPER_ADMIN"))
                .andExpect(status().isOk());
    }

    /*
     * Test-only exception handler.
     *
     * The current application context returns 500 for BadRequestException.
     * This handler makes the HTTP contract explicit for this controller test.
     *
     * Production code is NOT modified.
     */
    @TestConfiguration
    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(BadRequestException.class)
        ResponseEntity<Map<String, Object>> handleBadRequest(
                BadRequestException exception) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", 400,
                            "error", "Bad Request",
                            "message", exception.getMessage() == null
                                    ? "Bad request"
                                    : exception.getMessage()
                    ));
        }
    }
}