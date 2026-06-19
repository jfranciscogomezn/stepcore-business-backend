package com.stepcore.business.operations.controller.dto;

import java.util.List;

public record PortalOsiResponse(
        String osiNumber,
        String clientName,
        String origin,
        String destination,
        String aggregatedState,
        List<PortalEventResponse> events
) {}
