package com.epicode.buildweekbackend3.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(long id) {
        super("Elemento con id " + id + " non trovato!");
    }
}
