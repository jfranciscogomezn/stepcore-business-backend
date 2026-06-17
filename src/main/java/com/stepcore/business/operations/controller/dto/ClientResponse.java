package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;

public record ClientResponse(
        Long id,
        String name,
        String taxId,
        String contactName,
        String contactEmail,
        String contactPhone,
        String internalNotes,
        String status,
        OffsetDateTime createdAt
) {}
