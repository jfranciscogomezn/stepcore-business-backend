package com.stepcore.business.operations.service;

import com.stepcore.business.exception.ClientNotFoundException;
import com.stepcore.business.exception.OsiAlreadyClosedException;
import com.stepcore.business.exception.OsiNotFoundException;
import com.stepcore.business.operations.controller.dto.ChangeOsiOwnerRequest;
import com.stepcore.business.operations.controller.dto.CreateOsiRequest;
import com.stepcore.business.operations.controller.dto.OsiResponse;
import com.stepcore.business.operations.controller.dto.OsiSummaryResponse;
import com.stepcore.business.operations.controller.dto.OsiVehicleAssignmentResponse;
import com.stepcore.business.operations.controller.dto.UpdateOsiRequest;
import com.stepcore.business.operations.domain.model.Client;
import com.stepcore.business.operations.domain.model.Osi;
import com.stepcore.business.operations.domain.model.OsiStatus;
import com.stepcore.business.operations.domain.model.OsiVehicleAssignment;
import com.stepcore.business.operations.repository.ClientRepository;
import com.stepcore.business.operations.repository.OsiRepository;
import com.stepcore.business.operations.repository.OsiVehicleAssignmentRepository;
import com.stepcore.business.operations.repository.VehicleRepository;
import com.stepcore.business.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OsiServiceImpl implements OsiService {

    private final OsiRepository osiRepository;
    private final ClientRepository clientRepository;
    private final OsiVehicleAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public OsiResponse create(final CreateOsiRequest request, final Long coordinatorUserId) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        validateClient(tenantId, request.clientId());
        final String osiNumber = generateOsiNumber(tenantId);
        final Osi osi = osiRepository.save(Osi.builder()
                .withOsiNumber(osiNumber)
                .withClientId(request.clientId())
                .withOrigin(request.origin())
                .withDestination(request.destination())
                .withLoadWindowStart(request.loadWindowStart())
                .withLoadWindowEnd(request.loadWindowEnd())
                .withDeliveryWindowStart(request.deliveryWindowStart())
                .withDeliveryWindowEnd(request.deliveryWindowEnd())
                .withCommercialReference(request.commercialReference())
                .withInternalNotes(request.internalNotes())
                .withStatus(OsiStatus.DRAFT)
                .withCoordinatorUserId(coordinatorUserId)
                .build());
        return toResponse(osi, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OsiSummaryResponse> list(final String status, final String dateFrom,
                                          final String dateTo, final Pageable pageable) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        final Page<Osi> page;
        if (status != null && !status.isBlank()) {
            final OsiStatus osiStatus = OsiStatus.valueOf(status);
            if (dateFrom != null && dateTo != null) {
                final OffsetDateTime from = OffsetDateTime.parse(dateFrom, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                final OffsetDateTime to = OffsetDateTime.parse(dateTo, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                page = osiRepository.findByTenantIdAndStatusAndCreatedAtBetween(tenantId, osiStatus, from, to, pageable);
            } else {
                page = osiRepository.findByTenantIdAndStatus(tenantId, osiStatus, pageable);
            }
        } else {
            page = osiRepository.findByTenantId(tenantId, pageable);
        }
        return page.map(osi -> {
            final String clientName = clientRepository.findById(osi.getClientId())
                    .map(Client::getName).orElse("?");
            final int vehicleCount = (int) assignmentRepository.findByOsiId(osi.getId()).size();
            return toSummary(osi, clientName, vehicleCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public OsiResponse findById(final Long id) {
        final Osi osi = fetchOrThrow(id);
        final List<OsiVehicleAssignmentResponse> assignments = buildAssignmentResponses(osi.getId());
        return toResponse(osi, assignments);
    }

    @Override
    @Transactional
    public OsiResponse update(final Long id, final UpdateOsiRequest request) {
        final Osi osi = fetchOrThrow(id);
        if (osi.getStatus() == OsiStatus.CLOSED) {
            throw new OsiAlreadyClosedException(id);
        }
        if (request.clientId() != null) {
            validateClient(osi.getTenantId(), request.clientId());
            osi.setClientId(request.clientId());
        }
        if (request.origin() != null)               osi.setOrigin(request.origin());
        if (request.destination() != null)           osi.setDestination(request.destination());
        if (request.loadWindowStart() != null)       osi.setLoadWindowStart(request.loadWindowStart());
        if (request.loadWindowEnd() != null)         osi.setLoadWindowEnd(request.loadWindowEnd());
        if (request.deliveryWindowStart() != null)   osi.setDeliveryWindowStart(request.deliveryWindowStart());
        if (request.deliveryWindowEnd() != null)     osi.setDeliveryWindowEnd(request.deliveryWindowEnd());
        if (request.commercialReference() != null)   osi.setCommercialReference(request.commercialReference());
        if (request.internalNotes() != null)         osi.setInternalNotes(request.internalNotes());
        if (request.status() != null) {
            final OsiStatus newStatus = OsiStatus.valueOf(request.status());
            osi.setStatus(newStatus);
            if (newStatus == OsiStatus.CLOSED && osi.getClosedAt() == null) {
                osi.setClosedAt(OffsetDateTime.now());
            }
        }
        return toResponse(osiRepository.save(osi), buildAssignmentResponses(osi.getId()));
    }

    @Override
    @Transactional
    public OsiResponse changeOwner(final Long id, final ChangeOsiOwnerRequest request) {
        final Osi osi = fetchOrThrow(id);
        osi.setCoordinatorUserId(request.coordinatorUserId());
        return toResponse(osiRepository.save(osi), buildAssignmentResponses(osi.getId()));
    }

    private void validateClient(final Long tenantId, final Long clientId) {
        clientRepository.findById(clientId)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    private String generateOsiNumber(final Long tenantId) {
        final long count = osiRepository.countByTenantIdAndStatus(tenantId, OsiStatus.ACTIVE)
                + osiRepository.countByTenantIdAndStatus(tenantId, OsiStatus.DRAFT)
                + osiRepository.countByTenantIdAndStatus(tenantId, OsiStatus.CLOSED)
                + 1;
        return "OSI-" + tenantId + "-" + String.format("%06d", count);
    }

    private Osi fetchOrThrow(final Long id) {
        return osiRepository.findById(id).orElseThrow(() -> new OsiNotFoundException(id));
    }

    private List<OsiVehicleAssignmentResponse> buildAssignmentResponses(final Long osiId) {
        return assignmentRepository.findByOsiId(osiId).stream()
                .map(a -> {
                    final String plate = vehicleRepository.findById(a.getVehicleId())
                            .map(v -> v.getPlate()).orElse("?");
                    return toAssignmentResponse(a, plate);
                })
                .toList();
    }

    private OsiVehicleAssignmentResponse toAssignmentResponse(final OsiVehicleAssignment a, final String plate) {
        return new OsiVehicleAssignmentResponse(
                a.getId(), a.getVehicleId(), plate,
                a.getState().name(), a.getAssignedUserIds(),
                a.getCreatedAt(), a.getUpdatedAt());
    }

    private OsiSummaryResponse toSummary(final Osi o, final String clientName, final int vehicleCount) {
        return new OsiSummaryResponse(
                o.getId(), o.getOsiNumber(), o.getClientId(), clientName,
                o.getOrigin(), o.getDestination(),
                o.getStatus().name(), o.getCreatedAt(), vehicleCount);
    }

    private OsiResponse toResponse(final Osi o, final List<OsiVehicleAssignmentResponse> assignments) {
        final String clientName = clientRepository.findById(o.getClientId())
                .map(Client::getName).orElse("?");
        return new OsiResponse(
                o.getId(), o.getOsiNumber(), o.getClientId(), clientName,
                o.getOrigin(), o.getDestination(),
                o.getLoadWindowStart(), o.getLoadWindowEnd(),
                o.getDeliveryWindowStart(), o.getDeliveryWindowEnd(),
                o.getCommercialReference(), o.getInternalNotes(),
                o.getStatus().name(), o.getCoordinatorUserId(),
                o.getCreatedAt(), o.getClosedAt(), assignments);
    }
}
