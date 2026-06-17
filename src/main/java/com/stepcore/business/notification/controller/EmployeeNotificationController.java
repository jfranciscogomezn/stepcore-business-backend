package com.stepcore.business.notification.controller;

import com.stepcore.business.notification.controller.dto.EmployeeNotificationResponse;
import com.stepcore.business.notification.service.EmployeeNotificationService;
import com.stepcore.business.security.AppPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications/my")
@RequiredArgsConstructor
public class EmployeeNotificationController {

    private final EmployeeNotificationService employeeNotificationService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.MY_TIME + "')")
    public List<EmployeeNotificationResponse> listMine(final Authentication authentication) {
        try {
            final Long userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = ? LIMIT 1",
                    Long.class,
                    authentication.getName());
            if (userId == null) {
                return List.of();
            }
            return employeeNotificationService.listForUser(userId);
        } catch (final Exception ex) {
            return List.of();
        }
    }
}
