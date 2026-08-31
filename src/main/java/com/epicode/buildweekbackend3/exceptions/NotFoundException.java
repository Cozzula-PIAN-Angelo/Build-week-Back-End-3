package com.epicode.buildweekbackend3.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(long message) {
        super(String.valueOf(message));
    }
}
