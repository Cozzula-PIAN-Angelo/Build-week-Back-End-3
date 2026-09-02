package com.epicode.buildweekbackend3.payloads;

import com.epicode.buildweekbackend3.entities.Roles;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record NewInvoiceStatusDTO(
        @NotBlank(message = "Il nome dello stato è obbligatorio.")
        String name,
        String description,
        Roles requiredRole,
        Set<Long> allowedTransitionIds
) {
}