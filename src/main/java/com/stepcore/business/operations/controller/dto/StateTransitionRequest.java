package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record StateTransitionRequest(
        @NotBlank String targetState
) {}
