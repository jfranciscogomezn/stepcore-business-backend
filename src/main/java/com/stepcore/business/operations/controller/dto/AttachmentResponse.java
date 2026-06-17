package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;

public record AttachmentResponse(
        Long id,
        String filename,
        String uri,
        String mimeType,
        Long fileSizeBytes,
        OffsetDateTime createdAt
) {}
