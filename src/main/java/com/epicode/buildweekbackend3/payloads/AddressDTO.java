package com.epicode.buildweekbackend3.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressDTO(
        @NotBlank(message = "La via è obbligatoria")
        String street,

        @NotBlank(message = "Il civico è obbligatorio")
        String buildingNumber,

        @NotBlank(message = "La città è obbligatoria")
        String city,

        @NotBlank(message = "La provincia è obbligatoria")
        @Size(min = 2, max = 2, message = "La provincia deve essere esattamente di 2 lettere (es. RM)")
        String province,

        @NotBlank(message = "Il CAP è obbligatorio")
        @Pattern(regexp = "^\\d{5}$", message = "Il CAP deve contenere esattamente 5 cifre numeriche")
        String postalCode
) {
}