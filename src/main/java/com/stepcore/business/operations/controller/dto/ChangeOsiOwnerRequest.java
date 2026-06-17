package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeOsiOwnerRequest(
        @NotNull Long coordinatorUserId
) {}
