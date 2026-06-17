package com.stepcore.business.time.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.support.JwtTestSupport;
import com.stepcore.business.time.controller.dto.CreateCorrectionRequestRequest;
import com.stepcore.business.time.controller.dto.DismissCorrectionRequestRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the correction-request workflow endpoints.
 *
 * Scenario covered:
 *  1. Admin creates an employee whose email matches the employee JWT subject.
 *  2. Admin creates a time record for that employee (admin-create endpoint).
 *  3. Employee submits a correction request on that CLOSED record → HTTP 201, status=PENDING.
 *  4. Duplicate request on the same record → HTTP 409.
 *  5. Admin lists pending correction requests → HTTP 200, one entry.
 *  6. Admin dismisses the request with a reason → HTTP 200, status=DISMISSED.
 *  7. Admin attempts to dismiss an already-dismissed request → HTTP 409.
 *  8. Non-admin cannot list pending requests → HTTP 403.
 */
class CorrectionRequestIT extends BaseIntegrationTest {

    private static final Long TENANT_ID = 2L;
    private static final String EMPLOYEE_EMAIL = "employee@test.com";

    @Test
    void correctionRequestLifecycle() throws Exception {
        final String adminToken    = JwtTestSupport.adminToken(TENANT_ID);
        final String employeeToken = JwtTestSupport.token(
                EMPLOYEE_EMAIL, TENANT_ID,
                java.util.List.of("EMPLOYEE"),
                java.util.List.of("MY_TIME", "MY_PROFILE"));

        // ── 1. Create employee with the email that matches the employee JWT ──
        final String createEmployeeJson = """
                {
                  "firstName": "Test",
                  "lastName":  "Employee",
                  "idType":    "CC",
                  "idNumber":  "9999888",
                  "email":     "%s",
                  "phone":     null,
                  "monthlySalary": 3000000,
                  "userId":    null
                }
                """.formatted(EMPLOYEE_EMAIL);

        final MvcResult createEmpResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEmployeeJson))
                .andExpect(status().isCreated())
                .andReturn();

        final Long employeeId = objectMapper
                .readTree(createEmpResult.getResponse().getContentAsString())
                .get("id").asLong();

        // ── 2. Admin creates a CLOSED time record for the employee ──
        final String createRecordJson = """
                {
                  "employeeId":       %d,
                  "workDate":         "2025-06-01",
                  "clockIn":          "2025-06-01T08:00:00Z",
                  "clockOut":         "2025-06-01T17:00:00Z",
                  "correctionReason": "Initial admin creation"
                }
                """.formatted(employeeId);

        final MvcResult createRecordResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/time-records/correct")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRecordJson))
                .andExpect(status().isCreated())
                .andReturn();

        final Long timeRecordId = objectMapper
                .readTree(createRecordResult.getResponse().getContentAsString())
                .get("id").asLong();

        // ── 3. Employee submits correction request ──
        final MvcResult submitResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/time-records/" + timeRecordId + "/correction-request")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCorrectionRequestRequest("Clock-out was wrong"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        final Long requestId = objectMapper
                .readTree(submitResult.getResponse().getContentAsString())
                .get("id").asLong();

        // ── 4. Duplicate request on same record → 409 ──
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/time-records/" + timeRecordId + "/correction-request")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCorrectionRequestRequest("Another attempt"))))
                .andExpect(status().isConflict());

        // ── 5. Admin lists pending correction requests ──
        final MvcResult listResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/time-correction-requests")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andReturn();

        final JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(listJson.isArray()).isTrue();
        assertThat(listJson.size()).isGreaterThanOrEqualTo(1);

        final boolean requestFound = java.util.stream.StreamSupport.stream(listJson.spliterator(), false)
                .anyMatch(node -> node.get("id").asLong() == requestId);
        assertThat(requestFound).isTrue();

        // ── 6. Admin dismisses the request ──
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/time-correction-requests/" + requestId + "/dismiss")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DismissCorrectionRequestRequest("No correction needed"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"));

        // ── 7. Dismiss already-dismissed request → 409 ──
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/time-correction-requests/" + requestId + "/dismiss")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DismissCorrectionRequestRequest("Too late"))))
                .andExpect(status().isConflict());

        // ── 8. Non-admin cannot list pending requests → 403 ──
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/time-correction-requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .param("status", "PENDING"))
                .andExpect(status().isForbidden());
    }
}
