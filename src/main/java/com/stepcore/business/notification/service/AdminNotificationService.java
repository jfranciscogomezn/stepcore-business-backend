package com.stepcore.business.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepcore.business.notification.controller.dto.AdminNotificationResponse;
import com.stepcore.business.notification.domain.model.AdminNotification;
import com.stepcore.business.notification.model.IncompleteRecordNotificationItem;
import com.stepcore.business.notification.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;
    private final ObjectMapper objectMapper;

    public List<AdminNotificationResponse> listRecent() {
        return adminNotificationRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminNotification saveIncompleteTimeRecordsNotification(
            final String title,
            final String message,
            final List<IncompleteRecordNotificationItem> items) {
        final AdminNotification notification = AdminNotification.builder()
                .withNotificationType(AdminNotification.TYPE_INCOMPLETE_TIME_RECORDS)
                .withTitle(title)
                .withMessage(message)
                .withPayload(writePayload(items))
                .build();
        return adminNotificationRepository.save(notification);
    }

    @Transactional
    public AdminNotification saveCorrectionRequestNotification(final String title, final String message) {
        final AdminNotification notification = AdminNotification.builder()
                .withNotificationType(AdminNotification.TYPE_CORRECTION_REQUEST_SUBMITTED)
                .withTitle(title)
                .withMessage(message)
                .withPayload("[]")
                .build();
        return adminNotificationRepository.save(notification);
    }

    private AdminNotificationResponse toResponse(final AdminNotification notification) {
        return new AdminNotificationResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                readPayload(notification.getPayload()),
                notification.getCreatedAt());
    }

    private String writePayload(final List<IncompleteRecordNotificationItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (final JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize notification payload", exception);
        }
    }

    private List<IncompleteRecordNotificationItem> readPayload(final String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (final JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize notification payload", exception);
        }
    }
}
