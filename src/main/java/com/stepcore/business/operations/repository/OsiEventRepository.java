package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.OsiEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OsiEventRepository extends JpaRepository<OsiEvent, Long> {

    List<OsiEvent> findByOsiIdAndVehicleIdOrderByReceivedAtDesc(Long osiId, Long vehicleId);

    Optional<OsiEvent> findByTenantIdAndOsiIdAndVehicleIdAndIdempotencyKey(
            Long tenantId, Long osiId, Long vehicleId, UUID idempotencyKey);

    List<OsiEvent> findByParentEventIdOrderByReceivedAtAsc(Long parentEventId);
}
