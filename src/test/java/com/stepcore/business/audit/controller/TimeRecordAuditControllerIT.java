package com.stepcore.business.audit.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.support.EmployeeTestSupport;
import com.stepcore.business.support.JwtTestSupport;
import com.stepcore.business.time.controller.dto.CreateTimeRecordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimeRecordAuditControllerIT extends BaseIntegrationTest {

    private static final String ADMIN_TOKEN = JwtTestSupport.adminToken(2L);

    @Test
    void shouldListAuditEntriesAfterAdminCorrection() throws Exception {
        final String employeeBody = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/employees")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EmployeeTestSupport.validCreateRequest())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final Long employeeId = objectMapper.readTree(employeeBody).get("id").asLong();

        final CreateTimeRecordRequest recordRequest = new CreateTimeRecordRequest(
                employeeId,
                LocalDate.of(2026, 5, 20),
                Instant.parse("2026-05-20T13:00:00Z"),
                Instant.parse("2026-05-20T22:00:00Z"),
                "Manual entry for audit test");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/time-records/correct")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/audit/time-records")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("TIME_RECORD_CREATE"))
                .andExpect(jsonPath("$[0].actorEmail").value("admin@test.com"))
                .andExpect(jsonPath("$[0].details").value(org.hamcrest.Matchers.containsString("Manual entry")));
    }

    @Test
    void shouldRejectEmployeeFromAuditList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/audit/time-records")
                        .header("Authorization", "Bearer " + JwtTestSupport.employeeToken(2L)))
                .andExpect(status().isForbidden());
    }
}
