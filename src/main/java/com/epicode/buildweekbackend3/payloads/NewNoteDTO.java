package com.epicode.buildweekbackend3.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO di richiesta per creare/modificare una nota. Contiene solo il testo:
// cliente e autore non arrivano mai dal client, vengono ricavati dal path
// dell'URL (clientId) e dal JWT (autore), come da regola di progetto.
public record NewNoteDTO(
        @NotBlank(message = "Il testo della nota è obbligatorio")
        @Size(max = 2000, message = "Il testo della nota non può superare i 2000 caratteri")
        String text
) {
}
