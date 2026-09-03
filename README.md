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
- **Address** — via, civico, città, provincia, CAP; tipo (`LEGAL` / `OPERATIONAL`),
  cliente (→ Client)
- **Note** — testo, data di creazione, autore (→ User), cliente (→ Client)

### Indirizzi: gestione annidata nel Cliente (strada 1)

La consegna lascia scegliere tra due strade per gli indirizzi. Abbiamo
adottato la **strada 1**: un `Address` non ha una vita propria e si gestisce
solo attraverso il Cliente che lo possiede.

Motivazione:

- Nel dominio un indirizzo (sede legale / sede operativa) esiste **solo** in
  quanto sede di un cliente: `addresses.client_id` è `NOT NULL` e la coppia
  `(client_id, address_type)` è `UNIQUE`. Un `Address` orfano non è uno stato
  valido, quindi non ha senso esporne creazione ed eliminazione autonome.
- `legalAddress` e `operationalAddress` viaggiano **annidati** nel body di
  `POST` / `PUT /api/clients`. Se `operationalAddress` non è indicato, il
  service ne crea una copia dai dati di `legalAddress` (righe distinte anche
  se coincidono).
- Di conseguenza **non esiste un endpoint di modifica dell'indirizzo**: la
  modifica passa dal `PUT` sul Cliente. Questo tiene il controllo di
  competenza in un punto solo — `ClientsService.checkCanManage` (ADMIN
  sempre; COMMERCIALE solo sui clienti di cui è referente) — evitando una
  seconda porta d'accesso agli indirizzi con regole da mantenere allineate.

`GET /api/addresses` e `GET /api/addresses/{id}` restano disponibili in sola
lettura (nessuna restrizione di ruolo) come comodità di consultazione.

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

## Modulo Clienti

Un Client rappresenta un'azienda cliente di EPIC ENERGY SERVICES. Ogni Client ha
un indirizzo legale e uno operativo (entità `Address`, annidate nel payload), un
eventuale referente commerciale (`salesRep` → User) e una lista di Note e Fatture
collegate.

### Regole di autorizzazione

| Operazione | USER | COMMERCIALE | CONTABILE | ADMIN |
|---|:--:|:--:|:--:|:--:|
| `POST` (creazione) | ✅ | ✅ | ❌ | ✅ |
| `GET` (lista e dettaglio) | ✅ | ✅ | ✅ | ✅ |
| `PUT` (modifica) | ❌ | ✅ solo se referente del cliente | ❌ | ✅ sempre |
| `PATCH .../sales-rep` | ❌ | ❌ | ❌ | ✅ |
| `DELETE` | ❌ | ❌ | ❌ | ✅ |

Regole aggiuntive applicate nel service:

- **Assegnazione del referente in creazione**: se il cliente è creato da un
  `COMMERCIALE`, quel commerciale ne diventa automaticamente il referente
  (`salesRep`). Se lo crea uno `USER` o un `ADMIN`, il campo resta vuoto e la
  (ri)assegnazione avviene poi via `PATCH .../sales-rep` (solo ADMIN).
- **Proprietà in modifica**: un `COMMERCIALE` può modificare solo i clienti di
  cui è il referente assegnato; sugli altri riceve `403`. L'`ADMIN` non ha
  questa limitazione.
- **Tipo societario**: modificare `companyType` è riservato all'`ADMIN`. Un
  `COMMERCIALE` che tenta di cambiarlo in un `PUT` riceve `403` (se lo lascia
  invariato, il `PUT` procede).
- **Partita IVA ed email**: uniche tra tutti i clienti; un duplicato produce
  `400`.
- **Eliminazione**: un cliente con Note o Fatture collegate non è eliminabile →
  `400`.

### Endpoint

| Metodo | Endpoint | Descrizione |
|---|---|---|
| POST | `/api/clients` | Crea un cliente (con indirizzi annidati) — `201` |
| GET | `/api/clients` | Lista paginata e filtrabile |
| GET | `/api/clients/{clientId}` | Dettaglio di un cliente |
| PUT | `/api/clients/{clientId}` | Modifica anagrafica e indirizzi |
| PATCH | `/api/clients/{clientId}/sales-rep` | (Ri)assegna il referente commerciale — solo ADMIN |
| DELETE | `/api/clients/{clientId}` | Elimina un cliente — solo ADMIN, `204` |

### Parametri della lista (`GET /api/clients`)

Tutti opzionali, combinabili in AND.

| Parametro | Tipo | Filtra su |
|---|---|---|
| `page`, `size`, `sortBy` | int, int, string | Paginazione e ordinamento (default `0`, `10`, `id`) |
| `name` | string | Ragione sociale che **contiene** il valore (case-insensitive) |
| `revenueMin` / `revenueMax` | number | Fatturato annuo `≥` / `≤` (per l'uguaglianza esatta: `revenueMin=X&revenueMax=X`) |
| `insertedFrom` / `insertedTo` | date `YYYY-MM-DD` | Data di inserimento (`createdAt`), giornata inclusa per intero |
| `contactedFrom` / `contactedTo` | date `YYYY-MM-DD` | Data dell'ultimo contatto (`lastContactDate`) |

Esempio:
`GET /api/clients?name=acme&revenueMin=100000&contactedFrom=2026-08-01&sortBy=companyName`

### Request body (`POST` / `PUT`)

```json
{
  "companyName": "Acme S.r.l.",
  "vatNumber": "12345678901",
  "email": "info@acme.it",
  "annualRevenue": 150000.00,
  "companyType": "SRL",
  "legalAddress": {
    "street": "Via Roma",
    "buildingNumber": "10",
    "city": "Milano",
    "province": "MI",
    "postalCode": "20100"
  },
  "operationalAddress": null,
  "lastContactDate": "2026-08-20",
  "logoUrl": "https://esempio.com/acme-logo.png"
}
```

- `companyName`, `vatNumber` (11 cifre), `companyType` (`PA`, `SAS`, `SPA`,
  `SRL`) e `legalAddress` sono obbligatori.
- `email` (formato valido), `annualRevenue` (`≥ 0`), `lastContactDate` (non nel
  futuro) e `logoUrl` (URL valido) sono opzionali.
- `operationalAddress` opzionale: se assente, viene duplicato dall'indirizzo
  legale su una riga separata.

### Request body (`PATCH /api/clients/{clientId}/sales-rep`)

```json
{ "salesRepId": 3 }
```

- `salesRepId` deve essere l'id di un utente con ruolo `COMMERCIALE` → altrimenti
  `400`; utente inesistente → `404`.
- `"salesRepId": null` rimuove il referente dal cliente.

### Response (`POST` / `GET` / `PUT` / `PATCH`)

```json
{
  "id": 7,
  "companyName": "Acme S.r.l.",
  "vatNumber": "12345678901",
  "email": "info@acme.it",
  "annualRevenue": 150000.00,
  "companyType": "SRL",
  "logoUrl": "https://esempio.com/acme-logo.png",
  "lastContactDate": "2026-08-20",
  "createdAt": "2026-09-03T10:15:30",
  "updatedAt": "2026-09-03T10:15:30",
  "salesRep": { "id": 3, "name": "Mario", "surname": "Rossi", "email": "mario@epic.it", "role": "COMMERCIALE" },
  "legalAddress": { "id": 12, "street": "Via Roma", "buildingNumber": "10", "city": "Milano", "province": "MI", "postalCode": "20100" },
  "operationalAddress": { "id": 13, "street": "Via Roma", "buildingNumber": "10", "city": "Milano", "province": "MI", "postalCode": "20100" }
}
```

`GET /api/clients` restituisce lo stesso oggetto dentro la struttura `Page` di
Spring (`content`, `totalElements`, `totalPages`, `number`, …).

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
