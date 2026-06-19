package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TrackingTokenResponse(
        Long id,
        Long osiId,
        UUID token,
        Long createdByUserId,
        OffsetDateTime createdAt,
        OffsetDateTime revokedAt
) {}
