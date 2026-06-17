package com.stepcore.business.operations.controller.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TransportDocumentResponse(
        Long id,
        Long osiId,
        Long vehicleId,
        String type,
        String documentNumber,
        LocalDate documentDate,
        String adjunctUri,
        String internalNotes,
        OffsetDateTime createdAt
) {}
