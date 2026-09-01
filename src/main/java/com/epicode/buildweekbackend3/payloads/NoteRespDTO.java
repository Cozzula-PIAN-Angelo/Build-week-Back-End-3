package com.epicode.buildweekbackend3.payloads;

import com.epicode.buildweekbackend3.entities.Note;

import java.time.LocalDateTime;

// DTO di risposta per una nota: non esponiamo mai l'entity Note (e con
// essa User/Client) direttamente nelle risposte HTTP.
public record NoteRespDTO(
        long id,
        String text,
        LocalDateTime creationDate,
        long authorId,
        String authorFullName,
        long clientId
) {
    // Converte l'entity Note nel DTO da restituire al client, evitando di
    // ripetere questo mapping identico in ogni metodo del service.
    public static NoteRespDTO from(Note note) {
        return new NoteRespDTO(
                note.getId(),
                note.getText(),
                note.getCreationDate(),
                note.getAuthor().getId(),
                note.getAuthor().getName() + " " + note.getAuthor().getSurname(),
                note.getClient().getId()
        );
    }
}
