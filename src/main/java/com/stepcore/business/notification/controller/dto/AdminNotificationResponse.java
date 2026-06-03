package com.stepcore.business.notification.controller.dto;

import com.stepcore.business.notification.model.IncompleteRecordNotificationItem;

import java.time.Instant;
import java.util.List;

public record AdminNotificationResponse(
        Long id,
        String notificationType,
        String title,
        String message,
        List<IncompleteRecordNotificationItem> items,
        Instant createdAt
) {
}
