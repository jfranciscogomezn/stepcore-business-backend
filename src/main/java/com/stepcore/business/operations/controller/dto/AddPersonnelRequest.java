package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddPersonnelRequest(
        @NotEmpty List<Long> userIds
) {}
