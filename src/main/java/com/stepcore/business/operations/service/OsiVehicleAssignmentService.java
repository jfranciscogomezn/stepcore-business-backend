package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.AddPersonnelRequest;
import com.stepcore.business.operations.controller.dto.AssignVehicleRequest;
import com.stepcore.business.operations.controller.dto.OsiVehicleAssignmentResponse;
import com.stepcore.business.operations.controller.dto.StateTransitionRequest;

import java.util.List;

public interface OsiVehicleAssignmentService {

    OsiVehicleAssignmentResponse assign(Long osiId, AssignVehicleRequest request, Long coordinatorUserId);

    OsiVehicleAssignmentResponse transitionState(Long osiId, Long assignmentId, StateTransitionRequest request);

    OsiVehicleAssignmentResponse addPersonnel(Long osiId, Long assignmentId, AddPersonnelRequest request);

    List<OsiVehicleAssignmentResponse> listByOsi(Long osiId);
}
