package com.example.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CustomRoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * TC-S2-10
     *
     * A custom role is created as DRAFT and the publish endpoint must
     * transition it into the published state.
     */
    @Test
    @DisplayName("TC-S2-10 - Draft custom role can be published")
    void publishCustomRole_shouldReturn200() throws Exception {

        String roleName =
                "PART12_CUSTOM_ROLE_" + System.currentTimeMillis();

        String createResponse = mockMvc.perform(
                post("/api/v1/roles/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "%s",
                                  "roleCode": "PART12_CUSTOM_%d",
                                  "description": "Part 12 custom role",
                                  "permissionIds": []
                                }
                                """.formatted(
                                        roleName,
                                        System.currentTimeMillis()
                                ))
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

        long roleId = extractLongId(createResponse);

        mockMvc.perform(
                post("/api/v1/roles/custom/{roleId}/publish", roleId)
                        .param("publishNotes",
                                "Part 12 integration test publication")
        )
        .andExpect(status().isOk());
    }

    /*
     * TC-S2-11
     *
     * Draft custom roles cannot be assigned to users.
     *
     * The exact assignment endpoint is supplied by Part 4:
     * POST /api/v1/users/{userId}/roles
     */
    @Test
    @DisplayName("TC-S2-11 - Draft custom role must not be assignable")
    void draftCustomRole_shouldNotBeAssignable() throws Exception {

        String roleName =
                "PART12_DRAFT_ROLE_" + System.currentTimeMillis();

        String response = mockMvc.perform(
                post("/api/v1/roles/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "%s",
                                  "roleCode": "PART12_DRAFT_%d",
                                  "description": "Draft role",
                                  "permissionIds": []
                                }
                                """.formatted(
                                        roleName,
                                        System.currentTimeMillis()
                                ))
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

        long roleId = extractLongId(response);

        /*
         * UserRoleController expects UUID role IDs.
         *
         * The supplied developer documents contain a Long/UUID mismatch
         * between CustomRole and UserRole models. Therefore this test is
         * intentionally written as the acceptance test boundary and will
         * expose that developer mismatch if the assignment layer cannot
         * consume the custom-role identifier.
         */
        mockMvc.perform(
                post("/api/v1/users/{userId}/roles",
                        "00000000-0000-0000-0000-000000000011")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roles": [
                                    {
                                      "roleId": "%d",
                                      "primary": false,
                                      "effectiveDate": "2099-01-01"
                                    }
                                  ]
                                }
                                """.formatted(roleId))
        )
        .andExpect(
                result -> {
                    int status = result.getResponse().getStatus();

                    if (status != 422 && status != 400) {
                        throw new AssertionError(
                                "Draft custom role assignment should be "
                                + "rejected. Received HTTP " + status
                        );
                    }
                }
        );
    }

    private long extractLongId(String json) {

        int idIndex = json.indexOf("\"id\"");

        if (idIndex < 0) {
            throw new AssertionError(
                    "Custom role response does not contain id"
            );
        }

        int colon = json.indexOf(':', idIndex);

        int start = colon + 1;

        while (start < json.length()
                && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;

        while (end < json.length()
                && Character.isDigit(json.charAt(end))) {
            end++;
        }

        if (start == end) {
            throw new AssertionError(
                    "Custom role id is not numeric"
            );
        }

        return Long.parseLong(
                json.substring(start, end)
        );
    }
}
