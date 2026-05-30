package com.stepcore.business.payroll.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.payroll.controller.dto.HolidayRequest;
import com.stepcore.business.support.JwtTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HolidayControllerIT extends BaseIntegrationTest {

    private static final String BASE = "/api/v1/config/holidays";

    @Test
    void shouldManageHolidayCalendar() throws Exception {
        final String token = JwtTestSupport.adminToken(2L);
        final HolidayRequest request = new HolidayRequest(LocalDate.of(2026, 1, 1), "New Year");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-01-01"));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE + "/2026")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-01-01"));

        mockMvc.perform(MockMvcRequestBuilders.post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
