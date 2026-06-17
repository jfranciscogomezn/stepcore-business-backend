package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateOsiEventRequest(
        @NotNull Long eventTypeId,
        @NotBlank @Size(max = 2000) String text,
        OffsetDateTime capturedAtLocal,
        BigDecimal geoLat,
        BigDecimal geoLng,
        String externalPartyName,
        String externalPartyDocument,
        List<AddAttachmentRequest> attachments
) {}
