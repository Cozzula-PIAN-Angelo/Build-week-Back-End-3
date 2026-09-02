package com.epicode.buildweekbackend3.payloads;

import com.epicode.buildweekbackend3.entities.CompanyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record NewClientDTO(
        @NotBlank(message = "Il nome dell'azienda è obbligatorio")
        String companyName,

        @NotBlank(message = "La partita IVA è obbligatoria")
        @Pattern(regexp = "\\d{11}", message = "La partita IVA deve avere 11 cifre")
        String vatNumber,

        @Email(message = "L'indirizzo inserito non è un'email valida")
        String email,

        @PositiveOrZero(message = "Il fatturato annuo non può essere negativo")
        BigDecimal annualRevenue,

        @NotNull(message = "Il tipo di azienda è obbligatorio (PA, SAS, SPA, SRL)")
        CompanyType companyType,

        @NotNull(message = "L'indirizzo legale è obbligatorio")
        @Valid
        AddressDTO legalAddress,

        @Valid
        AddressDTO operationalAddress
) {
}
