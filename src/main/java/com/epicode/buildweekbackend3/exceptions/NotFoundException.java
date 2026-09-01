package com.epicode.buildweekbackend3.exceptions;

import java.util.UUID;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(UUID id) {
        super("Elemento con id " + id + " non trovato!");
    }

    public NotFoundException(long id) {
        super("Elemento con id " + id + " non trovato!");
    }
}