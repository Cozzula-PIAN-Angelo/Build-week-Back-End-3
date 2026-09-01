package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.entities.Note;
import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.ForbiddenException;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.payloads.NewNoteDTO;
import com.epicode.buildweekbackend3.payloads.NoteRespDTO;
import com.epicode.buildweekbackend3.repositories.ClientsRepository;
import com.epicode.buildweekbackend3.repositories.NotesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotesService {

    private final NotesRepository notesRepository;
    private final ClientsRepository clientsRepository;

    public NotesService(NotesRepository notesRepository, ClientsRepository clientsRepository) {
        this.notesRepository = notesRepository;
        this.clientsRepository = clientsRepository;
    }

    public NoteRespDTO create(long clientId, NewNoteDTO payload, User currentUser) {
        Client client = findClientOrThrow(clientId);
        checkAccess(client, currentUser);

        Note newNote = new Note(payload.text(), currentUser, client);
        return NoteRespDTO.from(this.notesRepository.save(newNote));
    }

    public List<NoteRespDTO> findByClient(long clientId, User currentUser) {
        Client client = findClientOrThrow(clientId);
        checkAccess(client, currentUser);

        return this.notesRepository.findByClientId(clientId).stream()
                .map(NoteRespDTO::from)
                .toList();
    }

    public NoteRespDTO findById(long noteId, User currentUser) {
        Note note = findNoteOrThrow(noteId);
        checkAccess(note.getClient(), currentUser);

        return NoteRespDTO.from(note);
    }

    public NoteRespDTO findByIdAndUpdate(long noteId, NewNoteDTO payload, User currentUser) {
        Note note = findNoteOrThrow(noteId);
        checkAccess(note.getClient(), currentUser);

        note.setText(payload.text());
        return NoteRespDTO.from(this.notesRepository.save(note));
    }

    public void findByIdAndDelete(long noteId, User currentUser) {
        Note note = findNoteOrThrow(noteId);
        checkAccess(note.getClient(), currentUser);

        this.notesRepository.delete(note);
    }

    private Client findClientOrThrow(long clientId) {
        return this.clientsRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException(clientId));
    }

    private Note findNoteOrThrow(long noteId) {
        return this.notesRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException(noteId));
    }

    // CREATE/READ/UPDATE/DELETE su Nota: ADMIN sempre; COMMERCIALE solo se
    // referente assegnato del cliente della nota; USER e CONTABILE mai.
    private void checkAccess(Client client, User currentUser) {
        if (currentUser.getRole() == Roles.ADMIN) return;

        if (currentUser.getRole() == Roles.COMMERCIALE
                && client.getSalesRep() != null
                && client.getSalesRep().getId().equals(currentUser.getId())) {
            return;
        }

        throw new ForbiddenException("Non hai i permessi per operare sulle note di questo cliente");
    }
}
