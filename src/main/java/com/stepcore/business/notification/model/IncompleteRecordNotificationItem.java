package com.stepcore.business.notification.model;

import java.time.LocalDate;

public record IncompleteRecordNotificationItem(
        Long employeeId,
        String employeeName,
        LocalDate workDate
) {
}
