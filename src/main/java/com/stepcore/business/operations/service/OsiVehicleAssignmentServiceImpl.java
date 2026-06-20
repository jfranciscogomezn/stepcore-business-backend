package com.stepcore.business.operations.service;

import com.stepcore.business.exception.ChecklistViolationException;
import com.stepcore.business.exception.HcValidationPendingException;
import com.stepcore.business.exception.OsiNotFoundException;
import com.stepcore.business.exception.VehicleNotFoundException;
import com.stepcore.business.exception.VehicleRetiredException;
import com.stepcore.business.notification.operations.OsiNotificationService;
import com.stepcore.business.operations.controller.dto.AddPersonnelRequest;
import com.stepcore.business.operations.controller.dto.AssignVehicleRequest;
import com.stepcore.business.operations.controller.dto.OsiVehicleAssignmentResponse;
import com.stepcore.business.operations.controller.dto.StateTransitionRequest;
import com.stepcore.business.operations.controller.dto.UpdateGpsRequest;
import com.stepcore.business.operations.controller.dto.UpdateHcValidationRequest;
import com.stepcore.business.operations.domain.model.HcValidationStatus;
import com.stepcore.business.operations.domain.model.OsiVehicleAssignment;
import com.stepcore.business.operations.domain.model.OsiVehicleState;
import com.stepcore.business.operations.domain.model.VehicleStatus;
import com.stepcore.business.operations.repository.OsiRepository;
import com.stepcore.business.operations.repository.OsiTransportDocumentRepository;
import com.stepcore.business.operations.repository.OsiVehicleAssignmentRepository;
import com.stepcore.business.operations.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsiVehicleAssignmentServiceImpl implements OsiVehicleAssignmentService {

    private final OsiVehicleAssignmentRepository assignmentRepository;
    private final OsiRepository osiRepository;
    private final VehicleRepository vehicleRepository;
    private final OsiVehicleStateMachine stateMachine;
    private final OsiTransportDocumentRepository documentRepository;
    private final OsiNotificationService osiNotificationService;

    @Value("${operations.hc-validation.block-cerrado-tracking:false}")
    private boolean blockCerradoTrackingOnHcPending;

    @Override
    @Transactional
    public OsiVehicleAssignmentResponse assign(final Long osiId, final AssignVehicleRequest request,
                                                final Long coordinatorUserId) {
        osiRepository.findById(osiId).orElseThrow(() -> new OsiNotFoundException(osiId));
        final var vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(request.vehicleId()));
        if (vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new VehicleRetiredException(request.vehicleId());
        }
        final List<Long> users = new ArrayList<>();
        if (request.assignedUserIds() != null) users.addAll(request.assignedUserIds());

        final OsiVehicleAssignment assignment = assignmentRepository.save(
                OsiVehicleAssignment.builder()
                        .withOsiId(osiId)
                        .withVehicleId(request.vehicleId())
                        .withState(OsiVehicleState.PLANNED)
                        .withAssignedUserIds(users)
                        .build());
        return toResponse(assignment, vehicle.getPlate());
    }

    @Override
    @Transactional
    public OsiVehicleAssignmentResponse transitionState(final Long osiId, final Long assignmentId,
                                                         final StateTransitionRequest request) {
        final OsiVehicleAssignment assignment = fetchOrThrow(assignmentId);
        final OsiVehicleState target = OsiVehicleState.valueOf(request.targetState());
        stateMachine.validate(assignment.getState(), target);

        if (target == OsiVehicleState.CERRADO_TRACKING) {
            final long docCount = documentRepository.countByOsiIdAndVehicleId(assignment.getOsiId(), assignment.getVehicleId());
            if (docCount == 0) {
                throw new ChecklistViolationException(
                        "Cannot close tracking: at least one transport document is required before closing assignment " + assignmentId);
            }
            if (blockCerradoTrackingOnHcPending
                    && assignment.getHcValidationStatus() == HcValidationStatus.PENDIENTE) {
                throw new HcValidationPendingException();
            }
        }

        assignment.setState(target);
        final String plate = vehicleRepository.findById(assignment.getVehicleId())
                .map(v -> v.getPlate()).orElse("?");
        return toResponse(assignmentRepository.save(assignment), plate);
    }

    @Override
    @Transactional
    public OsiVehicleAssignmentResponse addPersonnel(final Long osiId, final Long assignmentId,
                                                      final AddPersonnelRequest request) {
        final OsiVehicleAssignment assignment = fetchOrThrow(assignmentId);
        final List<Long> current = new ArrayList<>(assignment.getAssignedUserIds());
        request.userIds().forEach(uid -> {
            if (!current.contains(uid)) current.add(uid);
        });
        assignment.setAssignedUserIds(current);
        final String plate = vehicleRepository.findById(assignment.getVehicleId())
                .map(v -> v.getPlate()).orElse("?");
        return toResponse(assignmentRepository.save(assignment), plate);
    }

    @Override
    @Transactional
    public OsiVehicleAssignmentResponse updateGps(final Long osiId, final Long assignmentId,
                                                   final UpdateGpsRequest request) {
        final OsiVehicleAssignment assignment = fetchOrThrow(assignmentId);
        assignment.setGpsProvider(request.gpsProvider());
        assignment.setGpsReferenceUrl(request.gpsReferenceUrl());
        final String plate = vehicleRepository.findById(assignment.getVehicleId())
                .map(v -> v.getPlate()).orElse("?");
        return toResponse(assignmentRepository.save(assignment), plate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OsiVehicleAssignmentResponse> listByOsi(final Long osiId) {
        return assignmentRepository.findByOsiId(osiId).stream()
                .map(a -> {
                    final String plate = vehicleRepository.findById(a.getVehicleId())
                            .map(v -> v.getPlate()).orElse("?");
                    return toResponse(a, plate);
                }).toList();
    }

    @Override
    @Transactional
    public OsiVehicleAssignmentResponse updateHcValidation(final Long assignmentId,
                                                            final UpdateHcValidationRequest request,
                                                            final Long validatorUserId) {
        final OsiVehicleAssignment assignment = fetchOrThrow(assignmentId);
        final HcValidationStatus newStatus = HcValidationStatus.valueOf(request.status());
        assignment.setHcValidationStatus(newStatus);
        assignment.setHcValidationNotes(request.notes());
        assignment.setHcValidatedByUserId(validatorUserId);
        assignment.setHcValidatedAt(OffsetDateTime.now());
        final String plate = vehicleRepository.findById(assignment.getVehicleId())
                .map(v -> v.getPlate()).orElse("?");
        final OsiVehicleAssignment saved = assignmentRepository.save(assignment);

        if (newStatus == HcValidationStatus.RECHAZADO) {
            try {
                final String osiNumber = osiRepository.findById(assignment.getOsiId())
                        .map(o -> o.getOsiNumber()).orElse("#" + assignment.getOsiId());
                osiNotificationService.notifyHcRejected(assignment.getOsiId(), osiNumber, assignmentId);
            } catch (final Exception ex) {
                log.warn("Failed to emit HC-rejected notification for OSI {}: {}", assignment.getOsiId(), ex.getMessage());
            }
        }

        return toResponse(saved, plate);
    }

    private OsiVehicleAssignment fetchOrThrow(final Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
    }

    private OsiVehicleAssignmentResponse toResponse(final OsiVehicleAssignment a, final String plate) {
        return new OsiVehicleAssignmentResponse(
                a.getId(), a.getVehicleId(), plate,
                a.getState().name(), a.getAssignedUserIds(),
                a.getGpsProvider(), a.getGpsReferenceUrl(),
                a.getHcValidationStatus() != null ? a.getHcValidationStatus().name() : "PENDIENTE",
                a.getHcValidationNotes(),
                a.getHcValidatedByUserId(),
                a.getHcValidatedAt(),
                a.getCreatedAt(), a.getUpdatedAt());
    }
}
