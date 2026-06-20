package com.stepcore.business.notification.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepcore.business.notification.domain.model.AdminNotification;
import com.stepcore.business.notification.repository.AdminNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsiNotificationServiceTest {

    @Mock private AdminNotificationRepository repository;
    private OsiNotificationService service;

    @BeforeEach
    void setUp() {
        service = new OsiNotificationService(repository, new ObjectMapper());
    }

    @Test
    void notifyApprovalPending_savesCorrectType() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.notifyApprovalPending(42L, "OSI-2026-000042");

        final ArgumentCaptor<AdminNotification> captor = ArgumentCaptor.forClass(AdminNotification.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(AdminNotification.TYPE_OSI_APPROVAL_PENDING);
        assertThat(captor.getValue().getTitle()).contains("OSI-2026-000042");
        assertThat(captor.getValue().getPayload()).contains("42");
    }

    @Test
    void notifyHcRejected_savesCorrectType() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.notifyHcRejected(99L, "OSI-2026-000099", 5L);

        final ArgumentCaptor<AdminNotification> captor = ArgumentCaptor.forClass(AdminNotification.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(AdminNotification.TYPE_OSI_HC_REJECTED);
        assertThat(captor.getValue().getMessage()).contains("5");
    }

    @Test
    void listRecent_returnsOnlyOsiTypes() {
        final AdminNotification n = buildNotification(AdminNotification.TYPE_OSI_APPROVAL_PENDING,
                "OSI-2026-000001", 1L);
        when(repository.findTop20ByNotificationTypeInOrderByCreatedAtDesc(anyCollection()))
                .thenReturn(List.of(n));

        final List<OsiNotificationResponse> result = service.listRecent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).notificationType()).isEqualTo(AdminNotification.TYPE_OSI_APPROVAL_PENDING);
        assertThat(result.get(0).osiNumber()).isEqualTo("OSI-2026-000001");
    }

    private AdminNotification buildNotification(final String type, final String osiNumber, final Long osiId) {
        return AdminNotification.builder()
                .withNotificationType(type)
                .withTitle("Test")
                .withMessage("msg")
                .withPayload("{\"osiId\":" + osiId + ",\"osiNumber\":\"" + osiNumber + "\"}")
                .build();
    }
}
