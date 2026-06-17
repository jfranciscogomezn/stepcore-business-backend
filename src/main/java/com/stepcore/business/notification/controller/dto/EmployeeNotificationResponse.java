package com.stepcore.business.notification.controller.dto;

import java.time.Instant;

public record EmployeeNotificationResponse(
        Long id,
        String notificationType,
        String title,
        String message,
        boolean read,
        Instant createdAt
) {
}
