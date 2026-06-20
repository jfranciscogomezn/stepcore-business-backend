package com.stepcore.business.operations.service;

import com.stepcore.business.exception.HcValidationPendingException;
import com.stepcore.business.operations.controller.dto.StateTransitionRequest;
import com.stepcore.business.operations.controller.dto.UpdateHcValidationRequest;
import com.stepcore.business.operations.domain.model.HcValidationStatus;
import com.stepcore.business.operations.domain.model.OsiVehicleAssignment;
import com.stepcore.business.operations.domain.model.OsiVehicleState;
import com.stepcore.business.operations.repository.OsiRepository;
import com.stepcore.business.operations.repository.OsiTransportDocumentRepository;
import com.stepcore.business.operations.repository.OsiVehicleAssignmentRepository;
import com.stepcore.business.operations.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsiVehicleAssignmentServiceHcTest {

    @Mock private OsiVehicleAssignmentRepository assignmentRepository;
    @Mock private OsiRepository osiRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private OsiVehicleStateMachine stateMachine;
    @Mock private OsiTransportDocumentRepository documentRepository;
    @Mock private com.stepcore.business.notification.operations.OsiNotificationService osiNotificationService;

    private OsiVehicleAssignmentServiceImpl service;

    private OsiVehicleAssignment buildAssignment(final HcValidationStatus hcStatus) {
        final OsiVehicleAssignment a = new OsiVehicleAssignment();
        a.setId(1L);
        a.setOsiId(10L);
        a.setVehicleId(20L);
        a.setState(OsiVehicleState.DESCARGANDO);
        a.setHcValidationStatus(hcStatus);
        a.setAssignedUserIds(List.of());
        return a;
    }

    @BeforeEach
    void setUp() {
        service = new OsiVehicleAssignmentServiceImpl(
                assignmentRepository, osiRepository, vehicleRepository, stateMachine, documentRepository, osiNotificationService);
    }

    @Test
    void updateHcValidation_persistsStatusAndAuditFields() {
        final OsiVehicleAssignment assignment = buildAssignment(HcValidationStatus.PENDIENTE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(vehicleRepository.findById(20L)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        final var response = service.updateHcValidation(1L,
                new UpdateHcValidationRequest("VALIDADO", "All docs OK"), 99L);

        assertThat(response.hcValidationStatus()).isEqualTo("VALIDADO");
        assertThat(response.hcValidatedByUserId()).isEqualTo(99L);
        assertThat(response.hcValidatedAt()).isNotNull();
    }

    @Test
    void transitionState_blockedWhenHcPendingAndFlagEnabled() {
        ReflectionTestUtils.setField(service, "blockCerradoTrackingOnHcPending", true);

        final OsiVehicleAssignment assignment = buildAssignment(HcValidationStatus.PENDIENTE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(documentRepository.countByOsiIdAndVehicleId(10L, 20L)).thenReturn(1L);

        assertThatThrownBy(() -> service.transitionState(10L, 1L,
                new StateTransitionRequest("CERRADO_TRACKING")))
                .isInstanceOf(HcValidationPendingException.class);
    }

    @Test
    void transitionState_allowedWhenHcPendingAndFlagDisabled() {
        ReflectionTestUtils.setField(service, "blockCerradoTrackingOnHcPending", false);

        final OsiVehicleAssignment assignment = buildAssignment(HcValidationStatus.PENDIENTE);
        assignment.setState(OsiVehicleState.DESCARGANDO);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(documentRepository.countByOsiIdAndVehicleId(10L, 20L)).thenReturn(1L);
        when(vehicleRepository.findById(20L)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        final var response = service.transitionState(10L, 1L,
                new StateTransitionRequest("CERRADO_TRACKING"));

        assertThat(response.state()).isEqualTo("CERRADO_TRACKING");
    }
}
