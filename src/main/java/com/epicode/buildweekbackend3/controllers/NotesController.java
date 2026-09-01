package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.payloads.NewNoteDTO;
import com.epicode.buildweekbackend3.payloads.NoteRespDTO;
import com.epicode.buildweekbackend3.services.NotesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo mapping HTTP + delega al service: nessuna logica di autorizzazione
// o di business qui dentro (vive in NotesService).
@RestController
public class NotesController {

    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    // Crea una nota sul cliente {clientId}. currentUser arriva dal JWT
    // (impostato da JWTFilter), non è mai passato dal client.
    @PostMapping("/api/clients/{clientId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteRespDTO create(@PathVariable long clientId, @RequestBody @Valid NewNoteDTO payload,
                               @AuthenticationPrincipal User currentUser) {
        return this.notesService.create(clientId, payload, currentUser);
    }

    // Elenca le note di un cliente.
    @GetMapping("/api/clients/{clientId}/notes")
    public List<NoteRespDTO> getByClient(@PathVariable long clientId, @AuthenticationPrincipal User currentUser) {
        return this.notesService.findByClient(clientId, currentUser);
    }

    // Dettaglio di una singola nota.
    @GetMapping("/api/notes/{noteId}")
    public NoteRespDTO getById(@PathVariable long noteId, @AuthenticationPrincipal User currentUser) {
        return this.notesService.findById(noteId, currentUser);
    }

    // Modifica il testo di una nota esistente.
    @PutMapping("/api/notes/{noteId}")
    public NoteRespDTO update(@PathVariable long noteId, @RequestBody @Valid NewNoteDTO payload,
                               @AuthenticationPrincipal User currentUser) {
        return this.notesService.findByIdAndUpdate(noteId, payload, currentUser);
    }

    // Elimina una nota.
    @DeleteMapping("/api/notes/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long noteId, @AuthenticationPrincipal User currentUser) {
        this.notesService.findByIdAndDelete(noteId, currentUser);
    }
}
