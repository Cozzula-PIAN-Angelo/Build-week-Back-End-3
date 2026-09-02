package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotesRepository extends JpaRepository<Note, Long> {

    // Query derivata da Spring Data: trova tutte le note di un cliente
    // (naviga la relazione Note.client.id anche se il campo si chiama solo
    // "client" e non "clientId").
    List<Note> findByClientId(long clientId);

    boolean existsByClientId(long clientId);
}
