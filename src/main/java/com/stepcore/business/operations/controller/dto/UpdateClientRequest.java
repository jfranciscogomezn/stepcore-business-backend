package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateClientRequest(
        @Size(max = 150) String name,
        @Size(max = 30) String taxId,
        @Size(max = 150) String contactName,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 30) String contactPhone,
        String internalNotes,
        String status
) {}
