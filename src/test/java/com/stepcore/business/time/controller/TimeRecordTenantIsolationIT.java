package com.stepcore.business.time.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.support.EmployeeTestSupport;
import com.stepcore.business.support.JwtTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimeRecordTenantIsolationIT extends BaseIntegrationTest {

    @Test
    void tenantCannotReadAnotherTenantsEmployeeTimeRecords() throws Exception {
        final String tenantAToken = JwtTestSupport.adminToken(2L);
        final String tenantBToken = JwtTestSupport.adminToken(99L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/employees")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EmployeeTestSupport.validCreateRequest())))
                .andExpect(status().isCreated());

        final String body = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/employees")
                        .header("Authorization", "Bearer " + tenantAToken))
                .andReturn().getResponse().getContentAsString();
        final Long employeeId = objectMapper.readTree(body).get(0).get("id").asLong();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/time-records")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .param("employeeId", employeeId.toString())
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/time-records")
                        .header("Authorization", "Bearer " + tenantBToken)
                        .param("employeeId", employeeId.toString())
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isNotFound());
    }
}
