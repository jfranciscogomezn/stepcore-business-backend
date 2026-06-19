package com.stepcore.business.operations.controller.dto;

public record PortalAttachmentResponse(
        Long id,
        String filename,
        String uri,
        String mimeType
) {}
