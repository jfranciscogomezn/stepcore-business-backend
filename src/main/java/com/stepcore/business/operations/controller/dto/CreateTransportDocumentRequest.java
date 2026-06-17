package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateTransportDocumentRequest(
        @NotBlank String type,
        String documentNumber,
        LocalDate documentDate,
        String adjunctUri,
        String internalNotes
) {}
