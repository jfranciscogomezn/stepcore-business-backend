package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.CreateTransportDocumentRequest;
import com.stepcore.business.operations.controller.dto.TransportDocumentResponse;
import com.stepcore.business.operations.domain.model.OsiTransportDocument;
import com.stepcore.business.operations.repository.OsiTransportDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OsiTransportDocumentServiceImpl implements OsiTransportDocumentService {

    private final OsiTransportDocumentRepository documentRepository;

    @Override
    @Transactional
    public TransportDocumentResponse add(final Long osiId, final Long vehicleId,
                                          final CreateTransportDocumentRequest request) {
        final OsiTransportDocument doc = documentRepository.save(OsiTransportDocument.builder()
                .withOsiId(osiId)
                .withVehicleId(vehicleId)
                .withType(request.type())
                .withDocumentNumber(request.documentNumber())
                .withDocumentDate(request.documentDate())
                .withAdjunctUri(request.adjunctUri())
                .withInternalNotes(request.internalNotes())
                .build());
        return toResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportDocumentResponse> list(final Long osiId, final Long vehicleId) {
        return documentRepository.findByOsiIdAndVehicleIdOrderByCreatedAtDesc(osiId, vehicleId)
                .stream().map(this::toResponse).toList();
    }

    private TransportDocumentResponse toResponse(final OsiTransportDocument d) {
        return new TransportDocumentResponse(
                d.getId(), d.getOsiId(), d.getVehicleId(),
                d.getType(), d.getDocumentNumber(), d.getDocumentDate(),
                d.getAdjunctUri(), d.getInternalNotes(), d.getCreatedAt());
    }
}
