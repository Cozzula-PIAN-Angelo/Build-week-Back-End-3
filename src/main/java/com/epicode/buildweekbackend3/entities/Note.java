package com.epicode.buildweekbackend3.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// Nota testuale legata a un Cliente e scritta da uno User. Il controllo di
// accesso (chi può leggerla/modificarla) si basa sul Cliente, non
// sull'autore: la logica vera è in NotesService, non qui.
@Entity
@Table(name = "notes")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Note extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    // Chi ha scritto la nota. Va sempre impostato dall'utente autenticato
    // (JWT), mai da un valore passato nel body della richiesta.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Cliente a cui la nota si riferisce: è la chiave di tutto il controllo
    // di autorizzazione (referente assegnato = può operare sulla nota).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public Note(String text, User author, Client client) {
        this.text = text;
        this.author = author;
        this.client = client;
    }
}
