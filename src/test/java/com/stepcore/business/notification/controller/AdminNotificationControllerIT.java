package com.stepcore.business.notification.controller;

import com.stepcore.business.controller.BaseIntegrationTest;
import com.stepcore.business.notification.model.IncompleteRecordNotificationItem;
import com.stepcore.business.notification.service.AdminNotificationService;
import com.stepcore.business.support.JwtTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminNotificationControllerIT extends BaseIntegrationTest {

    @Autowired
    private AdminNotificationService adminNotificationService;

    @Test
    void shouldListRecentNotificationsForTimeRecordsAdmin() throws Exception {
        adminNotificationService.saveIncompleteTimeRecordsNotification(
                "Incomplete time records require attention",
                "1 time record(s) were marked INCOMPLETE",
                List.of(new IncompleteRecordNotificationItem(1L, "Ana Garcia", LocalDate.of(2026, 5, 29))));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + JwtTestSupport.adminToken(2L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationType").value("INCOMPLETE_TIME_RECORDS"))
                .andExpect(jsonPath("$[0].items[0].employeeName").value("Ana Garcia"));
    }

    @Test
    void shouldRejectEmployeeWithoutTimeRecordsAdminPermission() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + JwtTestSupport.employeeToken(2L)))
                .andExpect(status().isForbidden());
    }
}
