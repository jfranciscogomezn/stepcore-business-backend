package com.stepcore.business.payroll.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.payroll.controller.dto.PayrollConfigRequest;
import com.stepcore.business.support.JwtTestSupport;
import com.stepcore.business.support.PayrollConfigTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PayrollConfigControllerIT extends BaseIntegrationTest {

    private static final String BASE = "/api/v1/config/payroll";

    @Test
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE + "/2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectNonAdminRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE + "/2026")
                        .header("Authorization", "Bearer " + JwtTestSupport.employeeToken(2L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateAndRetrievePayrollConfiguration() throws Exception {
        final PayrollConfigRequest request = PayrollConfigTestSupport.validRequest();
        final String token = JwtTestSupport.adminToken(2L);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE + "/2026")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.minimumWage").value(1423500.00));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE + "/2026")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026));

        mockMvc.perform(MockMvcRequestBuilders.put(BASE + "/2026")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenConfigurationMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE + "/1999")
                        .header("Authorization", "Bearer " + JwtTestSupport.adminToken(2L)))
                .andExpect(status().isNotFound());
    }
}
