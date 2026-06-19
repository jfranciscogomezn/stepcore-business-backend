package com.stepcore.business.operations.controller.dto;

public record UpdateGpsRequest(
        String gpsProvider,
        String gpsReferenceUrl
) {}
