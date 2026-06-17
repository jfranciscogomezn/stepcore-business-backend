package com.stepcore.business.notification.service;

import com.stepcore.business.notification.controller.dto.EmployeeNotificationResponse;
import com.stepcore.business.notification.domain.model.EmployeeNotification;
import com.stepcore.business.notification.repository.EmployeeNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeNotificationService {

    private final EmployeeNotificationRepository employeeNotificationRepository;

    public List<EmployeeNotificationResponse> listForUser(final Long userId) {
        return employeeNotificationRepository
                .findTop20ByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EmployeeNotification save(
            final Long recipientUserId,
            final String notificationType,
            final String title,
            final String message) {
        final EmployeeNotification notification = EmployeeNotification.builder()
                .withRecipientUserId(recipientUserId)
                .withNotificationType(notificationType)
                .withTitle(title)
                .withMessage(message)
                .withRead(false)
                .build();
        return employeeNotificationRepository.save(notification);
    }

    private EmployeeNotificationResponse toResponse(final EmployeeNotification n) {
        return new EmployeeNotificationResponse(
                n.getId(),
                n.getNotificationType(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt());
    }
}
