package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.ForbiddenException;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.AddressDTO;
import com.epicode.buildweekbackend3.payloads.ClientFilterDTO;
import com.epicode.buildweekbackend3.payloads.NewClientDTO;
import com.epicode.buildweekbackend3.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ClientsService {

    private final ClientsRepository clientsRepository; // per salvare/leggere clienti
    private final NotesRepository notesRepository;
    private final InvoicesRepository invoicesRepository;
    private final UsersRepository usersRepository;

    public ClientsService(ClientsRepository clientsRepository, NotesRepository notesRepository, InvoicesRepository invoicesRepository, UsersRepository usersRepository) {
        this.clientsRepository = clientsRepository;
        this.notesRepository = notesRepository;
        this.invoicesRepository = invoicesRepository;
        this.usersRepository = usersRepository;
    }

    public Client create(NewClientDTO payload, User currentUser) {
        if (this.clientsRepository.existsByVatNumber(payload.vatNumber()))
            throw new ValidationException("La partita IVA " + payload.vatNumber() + " è già in uso");

        if (payload.email() != null && !payload.email().isBlank()
                && this.clientsRepository.existsByEmail(payload.email()))
            throw new ValidationException("L'email " + payload.email() + " e gia in uso");

        Client newClient = new Client(payload.companyName(), payload.vatNumber(), payload.companyType());
        newClient.setEmail(payload.email());
        newClient.setAnnualRevenue(payload.annualRevenue());
        newClient.setLastContactDate(payload.lastContactDate());
        newClient.setLogoUrl(payload.logoUrl());

        newClient.setLegalAddress(this.buildAddress(payload.legalAddress()));

        if (payload.operationalAddress() != null) {
            newClient.setOperationalAddress(this.buildAddress(payload.operationalAddress()));
        } else {
            // Nessuna sede operativa indicata: si presume coincida con la
            // sede legale. Address non può più essere condiviso tra due
            // "ruoli" (LEGAL/OPERATIONAL) sullo stesso record, quindi si
            // duplicano i dati in una riga a parte.
            newClient.setOperationalAddress(this.buildAddress(payload.legalAddress()));
        }

        if (currentUser.getRole() == Roles.COMMERCIALE) {
            newClient.setSalesRep(currentUser);
        } else {
            newClient.setSalesRep(null);
        }

        return this.clientsRepository.save(newClient);
    }

    private Address buildAddress(AddressDTO dto) {
        return Address.builder()
                .street(dto.street())
                .buildingNumber(dto.buildingNumber())
                .city(dto.city())
                .province(dto.province().toUpperCase())
                .postalCode(dto.postalCode())
                .build();
    }

    // findById ritorna un Optional<Client> (potrebbe esserci o no). orElseThrow significa "dammi il cliente, oppure lancia NotFoundException" → HTTP 404. Identico a
    //  UsersService.findById().
    public Client findById(long clientId) {
        return this.clientsRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException(clientId));
    }

    public Page<Client> findAll(int page, int size, String sortBy, ClientFilterDTO filter) {
        if (size > 100) size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // spec parte "sempre vero" (cb.conjunction), poi ci aggiungo in AND
        // solo i filtri che l'utente ha davvero passato. Un filtro null non
        // aggiunge niente: nella query finale quella condizione non esiste.
        Specification<Client> spec = (root, query, cb) -> cb.conjunction();
        if (filter.name() != null)          spec = spec.and(ClientSpecs.nameContains(filter.name()));
        if (filter.revenueMin() != null)    spec = spec.and(ClientSpecs.revenueAtLeast(filter.revenueMin()));
        if (filter.revenueMax() != null)    spec = spec.and(ClientSpecs.revenueAtMost(filter.revenueMax()));
        if (filter.insertedFrom() != null)  spec = spec.and(ClientSpecs.insertedFrom(filter.insertedFrom()));
        if (filter.insertedTo() != null)    spec = spec.and(ClientSpecs.insertedTo(filter.insertedTo()));
        if (filter.contactedFrom() != null) spec = spec.and(ClientSpecs.contactedFrom(filter.contactedFrom()));
        if (filter.contactedTo() != null)   spec = spec.and(ClientSpecs.contactedTo(filter.contactedTo()));

        return this.clientsRepository.findAll(spec, pageable);
    }

    public Client findByIdAndUpdate(long clientId, NewClientDTO payload, User currentUser) {
        Client clientFromDB = this.findById(clientId);

        this.checkCanManage(clientFromDB, currentUser);

        if (!clientFromDB.getVatNumber().equals(payload.vatNumber())
                && this.clientsRepository.existsByVatNumber(payload.vatNumber()))
            throw new ValidationException("La partita IVA " + payload.vatNumber() + " è già in uso");

        if (payload.email() != null && !payload.email().isBlank()
                && !payload.email().equalsIgnoreCase(clientFromDB.getEmail())
                && this.clientsRepository.existsByEmail(payload.email()))
            throw new ValidationException("L'email " + payload.email() + " è già in uso");

        if (currentUser.getRole() != Roles.ADMIN && clientFromDB.getCompanyType() != payload.companyType()) {
            throw new ForbiddenException("Solo un ADMIN può modificare il tipo societario.");
        }

        clientFromDB.setCompanyName(payload.companyName());
        clientFromDB.setVatNumber(payload.vatNumber());
        clientFromDB.setEmail(payload.email());
        clientFromDB.setAnnualRevenue(payload.annualRevenue());
        clientFromDB.setCompanyType(payload.companyType());
        clientFromDB.setLastContactDate(payload.lastContactDate());

        clientFromDB.setLegalAddress(this.buildAddress(payload.legalAddress()));
        clientFromDB.setOperationalAddress(
                payload.operationalAddress() != null
                        ? this.buildAddress(payload.operationalAddress())
                        : this.buildAddress(payload.legalAddress()));
        clientFromDB.setLogoUrl(payload.logoUrl());

        return this.clientsRepository.save(clientFromDB);
    }

    // void perché non c'è niente da restituire
    public void findByIdAndDelete(long clientId, User currentUser) {
        Client clientFromDB = this.findById(clientId);
        this.checkCanManage(clientFromDB, currentUser); // rete di sicurezza se un domani qualcuno allenta l'annotazione, e per coerenza con update.

        if (this.notesRepository.existsByClientId(clientId) || this.invoicesRepository.existsByClientId(clientId)) {
            throw new ValidationException("Impossibile eliminare il cliente: ci sono note o fatture collegate."); // errore 400
        }
        this.clientsRepository.delete(clientFromDB);
    }

    private void checkCanManage(Client client, User currentUser) {
        if (currentUser.getRole() == Roles.ADMIN) return;

        if (currentUser.getRole() == Roles.COMMERCIALE
                && client.getSalesRep() != null
                && client.getSalesRep().getId().equals(currentUser.getId())) {
            return;
        }

        throw new ForbiddenException("Non sei il referente di questo cliente");
    }

    public Client assignSalesRep(long clientId, Long salesRepId) {
        Client client = this.findById(clientId);
        // Riceve due id: quello del cliente da riassegnare (dall'URL) e quello del commerciale
        // a cui assegnarlo (dal body). Restituisce il Client aggiornato, così il controller lo
        //  rispedisce come JSON.

        if (salesRepId == null) {
            client.setSalesRep(null);
            return this.clientsRepository.save(client);
            // Se nel body non è stato passato un salesRepId (è null),
            // l'intenzione è "questo cliente non ha più un commerciale assegnato".
            //  - client.setSalesRep(null) → stacca la relazione (la colonna sales_rep_id diventa NULL)
            //  - save + return → salvo e esco subito dal metodo.
            //  Il return qui evita di eseguire tutto il resto (che presuppone un salesRepId valido).
        }

        User salesRep = this.usersRepository.findById(salesRepId).orElseThrow(() -> new NotFoundException(salesRepId));

        if (salesRep.getRole() != Roles.COMMERCIALE)
            throw new ValidationException("L'utente " + salesRepId + " non è un COMMERCIALE");

        client.setSalesRep(salesRep);
        return this.clientsRepository.save(client);
    }
}
