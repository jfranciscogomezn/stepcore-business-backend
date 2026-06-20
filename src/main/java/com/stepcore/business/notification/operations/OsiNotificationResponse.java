package com.stepcore.business.notification.operations;

import java.time.Instant;

public record OsiNotificationResponse(
        Long id,
        String notificationType,
        String title,
        String message,
        Long osiId,
        String osiNumber,
        Instant createdAt
) {}
