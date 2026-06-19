package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.OsiTransportDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OsiTransportDocumentRepository extends JpaRepository<OsiTransportDocument, Long> {

    List<OsiTransportDocument> findByOsiIdAndVehicleIdOrderByCreatedAtDesc(Long osiId, Long vehicleId);

    List<OsiTransportDocument> findByOsiIdOrderByCreatedAtDesc(Long osiId);

    long countByOsiIdAndVehicleId(Long osiId, Long vehicleId);
}
