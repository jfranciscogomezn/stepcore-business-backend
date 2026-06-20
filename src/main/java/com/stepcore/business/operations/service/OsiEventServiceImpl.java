package com.stepcore.business.operations.service;

import com.stepcore.business.exception.EventTypeNotFoundException;
import com.stepcore.business.exception.MaxAttachmentsExceededException;
import com.stepcore.business.exception.OsiEventNotFoundException;
import com.stepcore.business.notification.operations.OsiNotificationService;
import com.stepcore.business.operations.controller.dto.AddAttachmentRequest;
import com.stepcore.business.operations.controller.dto.AddCommentRequest;
import com.stepcore.business.operations.controller.dto.AttachmentResponse;
import com.stepcore.business.operations.controller.dto.CreateCorrectiveEventRequest;
import com.stepcore.business.operations.controller.dto.CreateOsiEventRequest;
import com.stepcore.business.operations.controller.dto.OsiEventResponse;
import com.stepcore.business.operations.controller.dto.PortalAttachmentResponse;
import com.stepcore.business.operations.controller.dto.PortalEventResponse;
import com.stepcore.business.operations.domain.model.EventType;
import com.stepcore.business.operations.domain.model.EventVisibility;
import com.stepcore.business.operations.domain.model.OsiEvent;
import com.stepcore.business.operations.domain.model.OsiEventAttachment;
import com.stepcore.business.operations.domain.model.OsiEventComment;
import com.stepcore.business.operations.repository.EventTypeRepository;
import com.stepcore.business.operations.repository.OsiEventAttachmentRepository;
import com.stepcore.business.operations.repository.OsiEventCommentRepository;
import com.stepcore.business.operations.repository.OsiEventRepository;
import com.stepcore.business.operations.repository.OsiRepository;
import com.stepcore.business.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsiEventServiceImpl implements OsiEventService {

    private final OsiEventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final OsiEventAttachmentRepository attachmentRepository;
    private final OsiEventCommentRepository commentRepository;
    private final OsiRepository osiRepository;
    private final OsiNotificationService osiNotificationService;

    @Override
    @Transactional
    public OsiEventResponse create(final Long osiId, final Long vehicleId,
                                    final CreateOsiEventRequest request,
                                    final Long authorUserId, final UUID idempotencyKey) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        final Optional<OsiEvent> existing = eventRepository
                .findByTenantIdAndOsiIdAndVehicleIdAndIdempotencyKey(tenantId, osiId, vehicleId, idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        final EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                .orElseThrow(() -> new EventTypeNotFoundException(request.eventTypeId()));

        final EventVisibility visibility = determineVisibility(eventType.getDefaultVisibility());

        final OsiEvent event = eventRepository.save(OsiEvent.builder()
                .withOsiId(osiId)
                .withVehicleId(vehicleId)
                .withEventTypeId(request.eventTypeId())
                .withAuthorUserId(authorUserId)
                .withText(request.text())
                .withCapturedAtLocal(request.capturedAtLocal())
                .withGeoLat(request.geoLat())
                .withGeoLng(request.geoLng())
                .withEffectiveVisibility(visibility)
                .withIdempotencyKey(idempotencyKey)
                .withExternalPartyName(request.externalPartyName())
                .withExternalPartyDocument(request.externalPartyDocument())
                .build());

        if (request.attachments() != null && !request.attachments().isEmpty()) {
            final int max = eventType.getMaxAttachments();
            if (request.attachments().size() > max) {
                throw new MaxAttachmentsExceededException(max);
            }
            for (final AddAttachmentRequest att : request.attachments()) {
                attachmentRepository.save(OsiEventAttachment.builder()
                        .withEventId(event.getId())
                        .withFilename(att.filename())
                        .withUri(att.uri())
                        .withMimeType(att.mimeType())
                        .withFileSizeBytes(att.fileSizeBytes())
                        .withChecksumSha256(att.checksumSha256())
                        .build());
            }
        }
        if (visibility == EventVisibility.PENDIENTE_APROBACION) {
            try {
                final String osiNumber = osiRepository.findById(osiId)
                        .map(o -> o.getOsiNumber()).orElse("#" + osiId);
                osiNotificationService.notifyApprovalPending(osiId, osiNumber);
            } catch (final Exception ex) {
                log.warn("Failed to emit approval-pending notification for OSI {}: {}", osiId, ex.getMessage());
            }
        }

        return toResponse(event);
    }

    @Override
    @Transactional
    public OsiEventResponse createCorrective(final Long osiId, final Long vehicleId,
                                              final Long parentEventId,
                                              final CreateCorrectiveEventRequest request,
                                              final Long authorUserId) {
        final OsiEvent parent = fetchOrThrow(parentEventId);
        final OsiEvent corrective = eventRepository.save(OsiEvent.builder()
                .withOsiId(osiId)
                .withVehicleId(vehicleId)
                .withEventTypeId(request.eventTypeId())
                .withAuthorUserId(authorUserId)
                .withText(request.text())
                .withEffectiveVisibility(EventVisibility.INTERNO)
                .withParentEventId(parent.getId())
                .withCorrectionReason(request.correctionReason())
                .withIdempotencyKey(UUID.randomUUID())
                .build());
        return toResponse(corrective);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsiEventResponse> list(final Long osiId, final Long vehicleId) {
        return eventRepository.findByOsiIdAndVehicleIdOrderByReceivedAtDesc(osiId, vehicleId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalEventResponse> listForPortal(final Long osiId) {
        final List<EventVisibility> clientVisible = List.of(EventVisibility.CLIENTE, EventVisibility.CLIENTE_CON_APROBACION);
        return eventRepository.findByOsiIdAndEffectiveVisibilityInOrderByReceivedAtAsc(osiId, clientVisible)
                .stream().map(this::toPortalResponse).toList();
    }

    @Override
    @Transactional
    public OsiEventResponse approveVisibility(final Long osiId, final Long vehicleId,
                                               final Long eventId, final Long approverUserId) {
        final OsiEvent event = fetchOrThrow(eventId);
        if (event.getEffectiveVisibility() == EventVisibility.PENDIENTE_APROBACION
                || event.getEffectiveVisibility() == EventVisibility.CLIENTE_CON_APROBACION) {
            event.setEffectiveVisibility(EventVisibility.CLIENTE);
        }
        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public OsiEventResponse addAttachment(final Long osiId, final Long vehicleId,
                                           final Long eventId, final AddAttachmentRequest request) {
        final OsiEvent event = fetchOrThrow(eventId);
        final EventType eventType = eventTypeRepository.findById(event.getEventTypeId())
                .orElseThrow(() -> new EventTypeNotFoundException(event.getEventTypeId()));
        final long current = attachmentRepository.countByEventId(eventId);
        if (current >= eventType.getMaxAttachments()) {
            throw new MaxAttachmentsExceededException(eventType.getMaxAttachments());
        }
        attachmentRepository.save(OsiEventAttachment.builder()
                .withEventId(eventId)
                .withFilename(request.filename())
                .withUri(request.uri())
                .withMimeType(request.mimeType())
                .withFileSizeBytes(request.fileSizeBytes())
                .withChecksumSha256(request.checksumSha256())
                .build());
        return toResponse(event);
    }

    @Override
    @Transactional
    public OsiEventResponse addComment(final Long osiId, final Long vehicleId,
                                        final Long eventId, final AddCommentRequest request,
                                        final Long authorUserId) {
        final OsiEvent event = fetchOrThrow(eventId);
        commentRepository.save(OsiEventComment.builder()
                .withEventId(eventId)
                .withAuthorUserId(authorUserId)
                .withText(request.text())
                .build());
        return toResponse(event);
    }

    private OsiEvent fetchOrThrow(final Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new OsiEventNotFoundException(id));
    }

    private EventVisibility determineVisibility(final EventVisibility defaultVis) {
        return switch (defaultVis) {
            case CLIENTE_CON_APROBACION -> EventVisibility.PENDIENTE_APROBACION;
            default -> defaultVis;
        };
    }

    private PortalEventResponse toPortalResponse(final OsiEvent e) {
        final List<PortalAttachmentResponse> atts = attachmentRepository
                .findByEventIdOrderByCreatedAtAsc(e.getId())
                .stream()
                .map(a -> new PortalAttachmentResponse(a.getId(), a.getFilename(), a.getUri(), a.getMimeType()))
                .toList();
        return new PortalEventResponse(
                e.getId(),
                eventTypeRepository.findById(e.getEventTypeId()).map(EventType::getName).orElse("?"),
                e.getText(),
                e.getCapturedAtLocal(),
                e.getReceivedAt(),
                e.getGeoLat(),
                e.getGeoLng(),
                atts);
    }

    private OsiEventResponse toResponse(final OsiEvent e) {
        final List<AttachmentResponse> attachments = attachmentRepository
                .findByEventIdOrderByCreatedAtAsc(e.getId())
                .stream()
                .map(a -> new AttachmentResponse(
                        a.getId(), a.getFilename(), a.getUri(),
                        a.getMimeType(), a.getFileSizeBytes(), a.getCreatedAt()))
                .toList();
        final String typeName = eventTypeRepository.findById(e.getEventTypeId())
                .map(EventType::getName).orElse("?");
        return new OsiEventResponse(
                e.getId(), e.getOsiId(), e.getVehicleId(),
                e.getEventTypeId(), typeName,
                e.getAuthorUserId(), e.getText(),
                e.getCapturedAtLocal(), e.getReceivedAt(),
                e.getGeoLat(), e.getGeoLng(),
                e.getEffectiveVisibility().name(),
                e.getParentEventId(), e.getCorrectionReason(),
                e.getIdempotencyKey(),
                e.getExternalPartyName(), e.getExternalPartyDocument(),
                attachments);
    }
}
