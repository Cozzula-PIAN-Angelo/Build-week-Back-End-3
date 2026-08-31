package com.epicode.buildweekbackend3.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewUserDTO(
        @NotBlank(message = "Il nome è obbligatorio")
        String name,

        @NotBlank(message = "Il cognome è obbligatorio")
        String surname,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "L'indirizzo inserito non è un email valida")
        String email,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 4, message = "La password deve essere di almeno 4 caratteri")
        String password
) {
}
