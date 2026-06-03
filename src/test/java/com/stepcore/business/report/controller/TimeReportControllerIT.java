package com.stepcore.business.report.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.notification.service.IncompleteTimeRecordJobService;
import com.stepcore.business.support.EmployeeTestSupport;
import com.stepcore.business.support.JwtTestSupport;
import com.stepcore.business.support.PayrollConfigTestSupport;
import com.stepcore.business.support.TimeReportTestSupport;
import com.stepcore.business.time.controller.dto.CreateTimeRecordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimeReportControllerIT extends BaseIntegrationTest {

    @Autowired
    private IncompleteTimeRecordJobService incompleteTimeRecordJobService;

    private static final String ADMIN_TOKEN = JwtTestSupport.adminToken(2L);
    private static final LocalDate WORK_DATE = LocalDate.of(2026, 5, 15);

    private Long employeeId;

    @BeforeEach
    void seedReportFixtures() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/config/payroll/2026")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PayrollConfigTestSupport.validRequest())))
                .andExpect(status().isCreated());

        final String employeeBody = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/employees")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EmployeeTestSupport.validCreateRequest())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        employeeId = objectMapper.readTree(employeeBody).get("id").asLong();

        final CreateTimeRecordRequest recordRequest = TimeReportTestSupport.closedRecord(employeeId, WORK_DATE);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/time-records/correct")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnCappedReportForMonth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/reports/time")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .param("employeeId", employeeId.toString())
                        .param("month", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(employeeId))
                .andExpect(jsonPath("$.capped").value(true))
                .andExpect(jsonPath("$.records[0].workDate").value("2026-05-15"))
                .andExpect(jsonPath("$.records[0].cappedEarnings").isNumber());
    }

    @Test
    void shouldReturnUncappedReportForAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/reports/time/uncapped")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .param("employeeId", employeeId.toString())
                        .param("date", WORK_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capped").value(false));
    }

    @Test
    void shouldExportExcelReport() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/reports/time/export")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .param("employeeId", employeeId.toString())
                        .param("month", "2026-05")
                        .param("cap", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("time-report.xlsx")));
    }

    @Test
    void shouldReturn409WhenPeriodContainsIncompleteRecords() throws Exception {
        final CreateTimeRecordRequest openRecord = new CreateTimeRecordRequest(
                employeeId,
                LocalDate.of(2026, 5, 16),
                java.time.Instant.parse("2026-05-16T13:00:00Z"),
                java.time.Instant.parse("2026-05-16T22:00:00Z"),
                "Will be marked incomplete");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/time-records/correct")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(openRecord)))
                .andExpect(status().isCreated());

        final String listBody = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/time-records")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .param("employeeId", employeeId.toString())
                        .param("from", "2026-05-16")
                        .param("to", "2026-05-16"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final Long recordId = objectMapper.readTree(listBody).get(0).get("id").asLong();

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/time-records/" + recordId + "/reopen")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk());

        incompleteTimeRecordJobService.processTenant(2L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/reports/time")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .param("employeeId", employeeId.toString())
                        .param("month", "2026-05"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.incompleteDates[0]").value("2026-05-16"));
    }
}
