package com.stepcore.business.operations.service;

import com.stepcore.business.exception.MaxAttachmentsExceededException;
import com.stepcore.business.operations.controller.dto.AddAttachmentRequest;
import com.stepcore.business.operations.controller.dto.CreateOsiEventRequest;
import com.stepcore.business.operations.domain.model.EventType;
import com.stepcore.business.operations.domain.model.EventVisibility;
import com.stepcore.business.operations.domain.model.OsiEvent;
import com.stepcore.business.operations.repository.EventTypeRepository;
import com.stepcore.business.operations.repository.OsiEventAttachmentRepository;
import com.stepcore.business.operations.repository.OsiEventCommentRepository;
import com.stepcore.business.operations.repository.OsiEventRepository;
import com.stepcore.business.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsiEventServiceTest {

    @Mock private OsiEventRepository eventRepository;
    @Mock private EventTypeRepository eventTypeRepository;
    @Mock private OsiEventAttachmentRepository attachmentRepository;
    @Mock private OsiEventCommentRepository commentRepository;
    private OsiEventServiceImpl service;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(2L);
        service = new OsiEventServiceImpl(eventRepository, eventTypeRepository,
                attachmentRepository, commentRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_idempotency_returnsSameEventIfKeyExists() {
        final UUID key = UUID.randomUUID();
        final OsiEvent existing = buildEvent(1L, EventVisibility.INTERNO);
        when(eventRepository.findByTenantIdAndOsiIdAndVehicleIdAndIdempotencyKey(2L, 10L, 20L, key))
                .thenReturn(Optional.of(existing));
        when(attachmentRepository.findByEventIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(eventTypeRepository.findById(5L)).thenReturn(Optional.of(buildEventType(5L, EventVisibility.INTERNO, 5)));

        final CreateOsiEventRequest req = new CreateOsiEventRequest(5L, "text", null, null, null, null, null, null);
        final var result = service.create(10L, 20L, req, 99L, key);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void create_clienteConAprobacion_setsPendingAprobacion() {
        final UUID key = UUID.randomUUID();
        when(eventRepository.findByTenantIdAndOsiIdAndVehicleIdAndIdempotencyKey(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        final EventType et = buildEventType(7L, EventVisibility.CLIENTE_CON_APROBACION, 5);
        when(eventTypeRepository.findById(7L)).thenReturn(Optional.of(et));
        final OsiEvent saved = buildEvent(2L, EventVisibility.PENDIENTE_APROBACION);
        when(eventRepository.save(any())).thenReturn(saved);
        when(attachmentRepository.findByEventIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(eventTypeRepository.findById(2L)).thenReturn(Optional.of(et));

        final CreateOsiEventRequest req = new CreateOsiEventRequest(7L, "event text", null, null, null, null, null, null);
        final var result = service.create(10L, 20L, req, 99L, key);

        assertThat(result.effectiveVisibility()).isEqualTo("PENDIENTE_APROBACION");
    }

    @Test
    void addAttachment_exceedsMax_throws() {
        final OsiEvent event = buildEvent(3L, EventVisibility.INTERNO);
        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));
        when(eventTypeRepository.findById(5L)).thenReturn(Optional.of(buildEventType(5L, EventVisibility.INTERNO, 2)));
        when(attachmentRepository.countByEventId(3L)).thenReturn(2L);

        assertThatThrownBy(() -> service.addAttachment(10L, 20L, 3L,
                new AddAttachmentRequest("file.pdf", "http://example.com/f", "application/pdf", null, null)))
                .isInstanceOf(MaxAttachmentsExceededException.class);
    }

    private OsiEvent buildEvent(final Long id, final EventVisibility vis) {
        final OsiEvent e = new OsiEvent();
        e.setId(id);
        e.setTenantId(2L);
        e.setOsiId(10L);
        e.setVehicleId(20L);
        e.setEventTypeId(5L);
        e.setAuthorUserId(99L);
        e.setText("text");
        e.setEffectiveVisibility(vis);
        e.setIdempotencyKey(UUID.randomUUID());
        e.setReceivedAt(OffsetDateTime.now());
        return e;
    }

    private EventType buildEventType(final Long id, final EventVisibility vis, final int maxAtt) {
        final EventType et = new EventType();
        et.setId(id);
        et.setTenantId(2L);
        et.setName("Test Type");
        et.setDefaultVisibility(vis);
        et.setMinAttachments(0);
        et.setMaxAttachments(maxAtt);
        et.setActive(true);
        et.setCreatedAt(OffsetDateTime.now());
        return et;
    }
}
