package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotesRepository extends JpaRepository<Note, Long> {
    List<Note> findByClientId(long clientId);
}
