package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.AddPersonnelRequest;
import com.stepcore.business.operations.controller.dto.AssignVehicleRequest;
import com.stepcore.business.operations.controller.dto.OsiVehicleAssignmentResponse;
import com.stepcore.business.operations.controller.dto.StateTransitionRequest;
import com.stepcore.business.operations.service.OsiVehicleAssignmentService;
import com.stepcore.business.operations.service.UserResolver;
import com.stepcore.business.security.AppPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations/osi/{osiId}/vehicles")
@RequiredArgsConstructor
public class OsiVehicleAssignmentController {

    private final OsiVehicleAssignmentService assignmentService;
    private final UserResolver userResolver;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public List<OsiVehicleAssignmentResponse> list(@PathVariable Long osiId) {
        return assignmentService.listByOsi(osiId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<OsiVehicleAssignmentResponse> assign(
            @PathVariable Long osiId,
            @Valid @RequestBody AssignVehicleRequest request,
            Authentication authentication) {
        final Long userId = userResolver.resolveByEmail(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.assign(osiId, request, userId));
    }

    @PatchMapping("/{assignmentId}/state")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public OsiVehicleAssignmentResponse transitionState(
            @PathVariable Long osiId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody StateTransitionRequest request) {
        return assignmentService.transitionState(osiId, assignmentId, request);
    }

    @PostMapping("/{assignmentId}/personnel")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public OsiVehicleAssignmentResponse addPersonnel(
            @PathVariable Long osiId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody AddPersonnelRequest request) {
        return assignmentService.addPersonnel(osiId, assignmentId, request);
    }
}
