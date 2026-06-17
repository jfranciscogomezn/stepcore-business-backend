package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.CreateTransportDocumentRequest;
import com.stepcore.business.operations.controller.dto.TransportDocumentResponse;

import java.util.List;

public interface OsiTransportDocumentService {

    TransportDocumentResponse add(Long osiId, Long vehicleId, CreateTransportDocumentRequest request);

    List<TransportDocumentResponse> list(Long osiId, Long vehicleId);
}
