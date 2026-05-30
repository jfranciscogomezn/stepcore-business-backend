package com.stepcore.business.payroll.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.support.JwtTestSupport;
import com.stepcore.business.support.PayrollConfigTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TenantIsolationIT extends BaseIntegrationTest {

    @Test
    void tenantCannotReadAnotherTenantsPayrollConfiguration() throws Exception {
        final String tenantAToken = JwtTestSupport.adminToken(2L);
        final String tenantBToken = JwtTestSupport.adminToken(99L);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/config/payroll/2026")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PayrollConfigTestSupport.validRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/config/payroll/2026")
                        .header("Authorization", "Bearer " + tenantBToken))
                .andExpect(status().isNotFound());
    }
}
