package com.stepcore.business.employee.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.employee.controller.dto.CreateEmployeeRequest;
import com.stepcore.business.employee.controller.dto.UpdateEmployeeRequest;
import com.stepcore.business.employee.domain.model.IdType;
import com.stepcore.business.support.EmployeeTestSupport;
import com.stepcore.business.support.JwtTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeControllerIT extends BaseIntegrationTest {

    private static final String BASE = "/api/v1/employees";

    @Test
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateListGetAndUpdateEmployee() throws Exception {
        final String token = JwtTestSupport.adminToken(2L);
        final CreateEmployeeRequest create = EmployeeTestSupport.validCreateRequest();

        mockMvc.perform(MockMvcRequestBuilders.post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.monthlySalary").value(3500000.00));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana.garcia@example.com"));

        final String body = mockMvc.perform(MockMvcRequestBuilders.get(BASE)
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        final Long id = objectMapper.readTree(body).get(0).get("id").asLong();

        mockMvc.perform(MockMvcRequestBuilders.get(BASE + "/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idNumber").value("1234567890"));

        final UpdateEmployeeRequest update = new UpdateEmployeeRequest(
                "Ana", "López", IdType.CC, "1234567890", "ana.garcia@example.com",
                "3001234567", new BigDecimal("3800000.00"), null);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE + "/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("López"));
    }

    @Test
    void shouldReturn409ForDuplicateDocument() throws Exception {
        final String token = JwtTestSupport.adminToken(2L);
        final CreateEmployeeRequest create = EmployeeTestSupport.validCreateRequest();

        mockMvc.perform(MockMvcRequestBuilders.post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated());

        final CreateEmployeeRequest duplicate = new CreateEmployeeRequest(
                "Bob", "Smith", IdType.CC, "1234567890", "bob@example.com",
                null, new BigDecimal("2000000"), null);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404ForMissingEmployee() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE + "/99999")
                        .header("Authorization", "Bearer " + JwtTestSupport.adminToken(2L)))
                .andExpect(status().isNotFound());
    }
}
