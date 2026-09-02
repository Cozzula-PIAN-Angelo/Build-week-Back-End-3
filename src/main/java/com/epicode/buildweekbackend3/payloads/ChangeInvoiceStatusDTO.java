package com.epicode.buildweekbackend3.payloads;

import jakarta.validation.constraints.NotNull;

public record ChangeInvoiceStatusDTO(
        @NotNull(message = "Il nuovo stato è obbligatorio.")
        Long newStatusId
) {
}