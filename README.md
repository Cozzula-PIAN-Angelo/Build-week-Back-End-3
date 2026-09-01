# EPIC ENERGY SERVICES — CRM Backend

Backend di un CRM per un'azienda fornitrice di energia che gestisce i contatti
con i clienti business. Progetto realizzato durante la build week EPICODE da
un team di 5 persone, ciascuna responsabile di un'area diversa del dominio.

Questo README copre la parte comune del progetto (setup, autenticazione,
gestione errori) e in dettaglio il modulo **Note**, di cui questo autore è
responsabile insieme alla parte trasversale (validazione, exception handler
globale, collection Postman).

## Stack tecnologico

- Java 25, Spring Boot 4.1.1
- Spring Web, Spring Data JPA, Spring Security (JWT)
- PostgreSQL
- Maven, Lombok

## Avvio del progetto

Prerequisiti: JDK 25, un database PostgreSQL raggiungibile con le credenziali
indicate in `src/main/resources/application.properties` (schema creato/
aggiornato automaticamente da Hibernate, `ddl-auto=update`).

```bash
./mvnw spring-boot:run
```

L'applicazione parte sulla porta `2462` (`server.port` in
`application.properties`).

## Autenticazione

Tutte le API tranne `/api/auth/**` richiedono un JWT nell'header
`Authorization: Bearer <token>`.

| Metodo | Endpoint             | Descrizione                          |
|--------|-----------------------|---------------------------------------|
| POST   | `/api/auth/register`  | Crea un nuovo utente (ruolo `USER`)  |
| POST   | `/api/auth/login`     | Restituisce l'access token JWT       |

Il ruolo di un utente può essere modificato via
`PATCH /api/users/{userId}/role` (payload `{ "roles": "COMMERCIALE" }`, valori
ammessi: `USER`, `COMMERCIALE`, `CONTABILE`, `ADMIN`).

## Modello dati (sintesi)

- **User** — email, password (BCrypt), nome, cognome, ruolo
- **Client** — ragione sociale, partita IVA, email, fatturato annuale, tipo
  societario, referente commerciale (→ User), indirizzo legale/operativo
  (→ Address)
- **Address** — via, civico, località, CAP, comune
- **Note** — testo, data di creazione, autore (→ User), cliente (→ Client)

> Nota di stato: al momento non esiste ancora un endpoint HTTP per creare
> Client/Address (in sviluppo su un altro branch del team); le entità e i
> repository esistono già lato service.

## Modulo Note

Una Nota è un appunto testuale legato a un Client e scritto da uno User
autenticato. L'autore e il cliente non vengono mai letti dal body della
richiesta: l'autore è sempre l'utente autenticato (dal JWT), il cliente è
identificato dal path dell'URL.

### Regole di autorizzazione

Il controllo di proprietà è basato sul **Client**, non sull'autore della
nota: un commerciale che è referente assegnato di un cliente vede e gestisce
tutte le note di quel cliente, comprese quelle scritte da un commerciale
precedente (es. dopo una riassegnazione).

| Ruolo         | CREATE | READ | UPDATE | DELETE |
|---------------|:------:|:----:|:------:|:------:|
| USER          | ❌ | ❌ | ❌ | ❌ |
| COMMERCIALE   | ✅ solo se referente del cliente | ✅ solo se referente | ✅ solo se referente | ✅ solo se referente |
| CONTABILE     | ❌ | ❌ | ❌ | ❌ |
| ADMIN         | ✅ sempre | ✅ sempre | ✅ sempre | ✅ sempre |

Le Note non compaiono mai, per nessun motivo, nella risposta a un CONTABILE
— nemmeno annidate dentro la risposta di un'altra risorsa (es. il dettaglio
di un Client). Questo filtro è applicato lato service, mai lato controller.

### Endpoint

| Metodo | Endpoint                          | Descrizione                              |
|--------|------------------------------------|--------------------------------------------|
| POST   | `/api/clients/{clientId}/notes`   | Crea una nota sul cliente indicato       |
| GET    | `/api/clients/{clientId}/notes`   | Elenca le note di un cliente             |
| GET    | `/api/notes/{noteId}`             | Dettaglio di una nota                     |
| PUT    | `/api/notes/{noteId}`             | Modifica il testo di una nota            |
| DELETE | `/api/notes/{noteId}`             | Elimina una nota                          |

**Request body** (`POST`/`PUT`):

```json
{
  "text": "Cliente interessato a un rinnovo contrattuale entro fine anno"
}
```

**Response** (`POST`/`GET`/`PUT`):

```json
{
  "id": 1,
  "text": "Cliente interessato a un rinnovo contrattuale entro fine anno",
  "creationDate": "2026-09-01T10:15:30",
  "authorId": 3,
  "authorFullName": "Mario Rossi",
  "clientId": 7
}
```

## Gestione errori

Un `@RestControllerAdvice` unico (`ExceptionsHandler`) intercetta tutte le
eccezioni custom del progetto e restituisce un payload coerente:

```json
{
  "message": "Non hai i permessi per operare sulle note di questo cliente",
  "timestamp": "2026-09-01T10:15:30"
}
```

| Eccezione             | Status HTTP | Quando                                              |
|------------------------|:-----------:|------------------------------------------------------|
| `ValidationException`  | 400         | Dati non validi (es. partita IVA già in uso)         |
| Errori `@Valid` sui DTO| 400         | Campo mancante o non conforme ai vincoli             |
| `UnauthorizedException`| 401         | Token mancante, scaduto o non valido                 |
| `ForbiddenException`, `AuthorizationDeniedException` | 403 | Utente autenticato ma senza i permessi per l'azione |
| `NotFoundException`    | 404         | Risorsa (Nota, Cliente, ...) inesistente             |
| Qualsiasi altra `Exception` | 500   | Errore imprevisto                                    |

## Collection Postman

`BuildWeek-BackEnd-3.postman_collection.json` nella root del repo contiene le
richieste di autenticazione e utenti; le richieste per il modulo Note vanno
aggiunte non appena sarà disponibile un `clientId` di test (endpoint Client
in arrivo da un altro membro del team).
