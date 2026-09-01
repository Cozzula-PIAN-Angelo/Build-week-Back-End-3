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

@RestController
public class NotesController {

    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @PostMapping("/api/clients/{clientId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteRespDTO create(@PathVariable long clientId, @RequestBody @Valid NewNoteDTO payload,
                               @AuthenticationPrincipal User currentUser) {
        return this.notesService.create(clientId, payload, currentUser);
    }

    @GetMapping("/api/clients/{clientId}/notes")
    public List<NoteRespDTO> getByClient(@PathVariable long clientId, @AuthenticationPrincipal User currentUser) {
        return this.notesService.findByClient(clientId, currentUser);
    }

    @GetMapping("/api/notes/{noteId}")
    public NoteRespDTO getById(@PathVariable long noteId, @AuthenticationPrincipal User currentUser) {
        return this.notesService.findById(noteId, currentUser);
    }

    @PutMapping("/api/notes/{noteId}")
    public NoteRespDTO update(@PathVariable long noteId, @RequestBody @Valid NewNoteDTO payload,
                               @AuthenticationPrincipal User currentUser) {
        return this.notesService.findByIdAndUpdate(noteId, payload, currentUser);
    }

    @DeleteMapping("/api/notes/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long noteId, @AuthenticationPrincipal User currentUser) {
        this.notesService.findByIdAndDelete(noteId, currentUser);
    }
}
