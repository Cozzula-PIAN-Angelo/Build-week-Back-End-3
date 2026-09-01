package com.epicode.buildweekbackend3.payloads;

import java.time.LocalDateTime;

// Formato unico di risposta per tutti gli errori gestiti da ExceptionsHandler.
public record ErrorResponseDTO(
        String message,
        LocalDateTime timestamp
) {
    // Costruttore comodo per non dover scrivere LocalDateTime.now() in ogni
    // punto dell'exception handler.
    public ErrorResponseDTO(String message) {
        this(message, LocalDateTime.now());
    }
}
