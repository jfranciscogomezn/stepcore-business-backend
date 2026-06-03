package com.stepcore.business.notification.controller;

import com.stepcore.business.notification.controller.dto.AdminNotificationResponse;
import com.stepcore.business.notification.service.AdminNotificationService;
import com.stepcore.business.security.AppPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public List<AdminNotificationResponse> listRecent() {
        return adminNotificationService.listRecent();
    }
}
