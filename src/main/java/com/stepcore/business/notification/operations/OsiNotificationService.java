package com.stepcore.business.notification.operations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepcore.business.notification.domain.model.AdminNotification;
import com.stepcore.business.notification.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsiNotificationService {

    private static final List<String> OSI_TYPES = List.of(
            AdminNotification.TYPE_OSI_APPROVAL_PENDING,
            AdminNotification.TYPE_OSI_HC_REJECTED
    );

    private final AdminNotificationRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyApprovalPending(final Long osiId, final String osiNumber) {
        save(AdminNotification.TYPE_OSI_APPROVAL_PENDING,
                "Evento pendiente de aprobación — " + osiNumber,
                "Se ha creado un evento con visibilidad CLIENTE que requiere aprobación del coordinador.",
                osiId, osiNumber);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyHcRejected(final Long osiId, final String osiNumber, final Long assignmentId) {
        save(AdminNotification.TYPE_OSI_HC_REJECTED,
                "Validación HC rechazada — " + osiNumber,
                "La asignación #" + assignmentId + " tiene documentación HC rechazada. Revise con el HC Validador.",
                osiId, osiNumber);
    }

    @Transactional(readOnly = true)
    public List<OsiNotificationResponse> listRecent() {
        return repository.findTop20ByNotificationTypeInOrderByCreatedAtDesc(OSI_TYPES)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void save(final String type, final String title, final String message,
                      final Long osiId, final String osiNumber) {
        final String payload = serialize(new OsiNotificationPayload(osiId, osiNumber));
        repository.save(AdminNotification.builder()
                .withNotificationType(type)
                .withTitle(title)
                .withMessage(message)
                .withPayload(payload)
                .build());
    }

    private OsiNotificationResponse toResponse(final AdminNotification n) {
        OsiNotificationPayload payload;
        try {
            payload = objectMapper.readValue(n.getPayload(), OsiNotificationPayload.class);
        } catch (final JsonProcessingException ex) {
            log.warn("Failed to parse osi notification payload id={}", n.getId(), ex);
            payload = new OsiNotificationPayload(null, "?");
        }
        return new OsiNotificationResponse(
                n.getId(), n.getNotificationType(), n.getTitle(), n.getMessage(),
                payload.osiId(), payload.osiNumber(), n.getCreatedAt());
    }

    private String serialize(final OsiNotificationPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (final JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize OsiNotificationPayload", ex);
        }
    }
}
