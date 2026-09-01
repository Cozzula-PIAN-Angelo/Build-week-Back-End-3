package com.epicode.buildweekbackend3.payloads;

import com.epicode.buildweekbackend3.entities.Note;

import java.time.LocalDateTime;

public record NoteRespDTO(
        long id,
        String text,
        LocalDateTime creationDate,
        long authorId,
        String authorFullName,
        long clientId
) {
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
