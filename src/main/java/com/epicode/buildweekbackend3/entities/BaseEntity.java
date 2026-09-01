package com.epicode.buildweekbackend3.entities;

// BaseEntity è la classe da cui Client (e tutte le altre entity: User, Address, Invoice, Note)
// ereditano tre cose che il progetto richiede per ognuna di esse: l'id, la data di creazione
// e la data di ultima modifica.

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass // l'id viene ereditato nelle tabelle di Client, Address, Invoice, Note
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
