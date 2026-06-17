package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.Vehicle;
import com.stepcore.business.operations.domain.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByTenantIdAndPlate(Long tenantId, String plate);

    List<Vehicle> findAllByTenantIdAndStatusOrderByPlateAsc(Long tenantId, VehicleStatus status);

    List<Vehicle> findAllByTenantIdOrderByPlateAsc(Long tenantId);
}
