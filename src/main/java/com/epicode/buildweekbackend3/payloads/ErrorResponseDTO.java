package com.epicode.buildweekbackend3.payloads;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponseDTO(String message) {
        this(message, LocalDateTime.now());
    }
}
