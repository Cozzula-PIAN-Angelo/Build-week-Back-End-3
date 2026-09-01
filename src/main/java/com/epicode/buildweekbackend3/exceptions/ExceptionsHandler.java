package com.epicode.buildweekbackend3.exceptions;

import com.epicode.buildweekbackend3.payloads.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

// Punto unico di gestione delle eccezioni per tutto il progetto (non solo
// per le Note): intercetta le eccezioni custom e restituisce sempre lo
// stesso formato di errore { message, timestamp } con lo status corretto.
@RestControllerAdvice
public class ExceptionsHandler {

    // 400 - dati "di business" non validi (es. partita IVA già in uso),
    // lanciata esplicitamente dai service.
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidationEx(ValidationException ex) {
        return new ErrorResponseDTO(ex.getMessage(), LocalDateTime.now());
    }

    // 400 - fallimento delle annotazioni di validazione sui DTO (@NotBlank,
    // @Email, ecc.) scatenato da @Valid nei controller. Concatena tutti i
    // messaggi di errore dei singoli campi in un'unica stringa.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidationEx(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ErrorResponseDTO(message, LocalDateTime.now());
    }

    // 401 - token JWT mancante, scaduto o non valido.
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDTO handleUnauthorized(UnauthorizedException ex) {
        return new ErrorResponseDTO(ex.getMessage(), LocalDateTime.now());
    }

    // 403 - utente autenticato ma senza i permessi per l'azione richiesta.
    // Gestisce insieme la nostra ForbiddenException (lanciata a mano nei
    // service, es. NotesService) e AuthorizationDeniedException (lanciata
    // automaticamente da Spring Security quando un @PreAuthorize fallisce).
    @ExceptionHandler({ForbiddenException.class, AuthorizationDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDTO handleForbiddenEx(RuntimeException ex) {
        String message = ex instanceof ForbiddenException ? ex.getMessage() : "Non hai i permessi per questa risorsa";
        return new ErrorResponseDTO(message, LocalDateTime.now());
    }

    // 404 - risorsa richiesta (nota, cliente, ...) inesistente. Il messaggio
    // completo viene gia' costruito da NotFoundException (o passato dai service).
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundEx(NotFoundException ex) {
        return new ErrorResponseDTO(ex.getMessage(), LocalDateTime.now());
    }

    // 404 - endpoint inesistente (nessuna rotta mappata su URL/metodo richiesti).
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNoResourceEx(NoResourceFoundException ex) {
        return new ErrorResponseDTO("Endpoint non trovato: " + ex.getResourcePath(), LocalDateTime.now());
    }

    // 500 - fallback per qualunque eccezione non prevista sopra: logga lo
    // stack trace lato server e restituisce un messaggio generico al client
    // (mai i dettagli interni dell'errore).
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleGenericEx(Exception ex) {
        ex.printStackTrace();
        return new ErrorResponseDTO("Errore interno del server! Ci stiamo lavorando.", LocalDateTime.now());
    }
}
