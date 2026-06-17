package com.stepcore.business.operations.service;

import com.stepcore.business.exception.EventTypeNotFoundException;
import com.stepcore.business.operations.controller.dto.CreateEventTypeRequest;
import com.stepcore.business.operations.controller.dto.EventTypeResponse;
import com.stepcore.business.operations.controller.dto.UpdateEventTypeRequest;
import com.stepcore.business.operations.domain.model.EventType;
import com.stepcore.business.operations.domain.model.EventVisibility;
import com.stepcore.business.operations.repository.EventTypeRepository;
import com.stepcore.business.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventTypeServiceImpl implements EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    @Override
    @Transactional
    public EventTypeResponse create(final CreateEventTypeRequest request) {
        final EventVisibility visibility = parseVisibility(request.defaultVisibility());
        final EventType eventType = eventTypeRepository.save(EventType.builder()
                .withName(request.name())
                .withDescription(request.description())
                .withDefaultVisibility(visibility)
                .withMinAttachments(request.minAttachments())
                .withMaxAttachments(request.maxAttachments())
                .withHasMeasurementForm(request.hasMeasurementForm())
                .withActive(true)
                .build());
        return toResponse(eventType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTypeResponse> findAll(final boolean activeOnly) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        return (activeOnly
                ? eventTypeRepository.findByTenantIdAndActiveOrderByNameAsc(tenantId, true)
                : eventTypeRepository.findByTenantIdOrderByNameAsc(tenantId))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventTypeResponse findById(final Long id) {
        return toResponse(fetchOrThrow(id));
    }

    @Override
    @Transactional
    public EventTypeResponse update(final Long id, final UpdateEventTypeRequest request) {
        final EventType et = fetchOrThrow(id);
        if (request.name() != null)                et.setName(request.name());
        if (request.description() != null)         et.setDescription(request.description());
        if (request.defaultVisibility() != null)   et.setDefaultVisibility(parseVisibility(request.defaultVisibility()));
        if (request.minAttachments() != null)      et.setMinAttachments(request.minAttachments());
        if (request.maxAttachments() != null)      et.setMaxAttachments(request.maxAttachments());
        if (request.hasMeasurementForm() != null)  et.setHasMeasurementForm(request.hasMeasurementForm());
        if (request.active() != null)              et.setActive(request.active());
        return toResponse(eventTypeRepository.save(et));
    }

    private EventType fetchOrThrow(final Long id) {
        return eventTypeRepository.findById(id)
                .orElseThrow(() -> new EventTypeNotFoundException(id));
    }

    private EventVisibility parseVisibility(final String v) {
        if (v == null) return EventVisibility.INTERNO;
        try {
            return EventVisibility.valueOf(v);
        } catch (IllegalArgumentException e) {
            return EventVisibility.INTERNO;
        }
    }

    private EventTypeResponse toResponse(final EventType et) {
        return new EventTypeResponse(
                et.getId(), et.getName(), et.getDescription(),
                et.getDefaultVisibility().name(),
                et.getMinAttachments(), et.getMaxAttachments(),
                et.isHasMeasurementForm(), et.isActive(), et.getCreatedAt());
    }
}
