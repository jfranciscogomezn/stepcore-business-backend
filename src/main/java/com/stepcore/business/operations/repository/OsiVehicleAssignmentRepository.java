package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.OsiVehicleAssignment;
import com.stepcore.business.operations.domain.model.OsiVehicleState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OsiVehicleAssignmentRepository extends JpaRepository<OsiVehicleAssignment, Long> {

    List<OsiVehicleAssignment> findByOsiId(Long osiId);

    Optional<OsiVehicleAssignment> findByOsiIdAndVehicleId(Long osiId, Long vehicleId);

    List<OsiVehicleAssignment> findByOsiIdAndStateNot(Long osiId, OsiVehicleState state);

    long countByOsiIdAndStateNot(Long osiId, OsiVehicleState state);
}
