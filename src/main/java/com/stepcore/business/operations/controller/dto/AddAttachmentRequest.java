package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record AddAttachmentRequest(
        @NotBlank String filename,
        @NotBlank String uri,
        String mimeType,
        Long fileSizeBytes,
        String checksumSha256
) {}
