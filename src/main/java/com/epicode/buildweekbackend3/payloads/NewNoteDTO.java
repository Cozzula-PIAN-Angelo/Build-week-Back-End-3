package com.epicode.buildweekbackend3.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewNoteDTO(
        @NotBlank(message = "Il testo della nota è obbligatorio")
        @Size(max = 2000, message = "Il testo della nota non può superare i 2000 caratteri")
        String text
) {
}
