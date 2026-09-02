package com.epicode.buildweekbackend3.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NewInvoiceDTO(
        @NotBlank(message = "Il numero della fattura è obbligatorio.")
        String invoiceNumber,

        @NotNull(message = "La data della fattura è obbligatoria.")
        LocalDate invoiceDate,

        @NotNull(message = "L'importo è obbligatorio.")
        @Positive(message = "L'importo deve essere maggiore di zero.")
        BigDecimal amount,

        @NotNull(message = "Il cliente è obbligatorio.")
        Long clientId
) {
}