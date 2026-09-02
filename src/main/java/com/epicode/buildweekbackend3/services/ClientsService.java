package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.ForbiddenException;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.AddressDTO;
import com.epicode.buildweekbackend3.payloads.NewClientDTO;
import com.epicode.buildweekbackend3.repositories.ClientsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ClientsService {

    private final ClientsRepository clientsRepository; // per salvare/leggere clienti

    public ClientsService(ClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public Client create(NewClientDTO payload, User currentUser) {
        if (this.clientsRepository.existsByVatNumber(payload.vatNumber()))
            throw new ValidationException("La partita IVA " + payload.vatNumber() + " è già in uso");

        Client newClient = new Client(payload.companyName(), payload.vatNumber(), payload.companyType());
        newClient.setEmail(payload.email());
        newClient.setAnnualRevenue(payload.annualRevenue());

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

    public Page<Client> findAll(int page, int size, String sortBy) {
        if (size > 100) size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return this.clientsRepository.findAll(pageable);
    }

    public Client findByIdAndUpdate(long clientId, NewClientDTO payload, User currentUser) {
        Client clientFromDB = this.findById(clientId);

        if(currentUser.getRole() == Roles.COMMERCIALE) {
            if (clientFromDB.getSalesRep() != null) {
                throw new ForbiddenException("Non sei autorizzato a modificare questo Cliente");
            }
        }

        if (!clientFromDB.getVatNumber().equals(payload.vatNumber())
                && this.clientsRepository.existsByVatNumber(payload.vatNumber()))
            throw new ValidationException("La partita IVA " + payload.vatNumber() + " è già in uso");

        clientFromDB.setCompanyName(payload.companyName());
        clientFromDB.setVatNumber(payload.vatNumber());
        clientFromDB.setEmail(payload.email());
        clientFromDB.setAnnualRevenue(payload.annualRevenue());
        clientFromDB.setCompanyType(payload.companyType());

        clientFromDB.setLegalAddress(this.buildAddress(payload.legalAddress()));
        clientFromDB.setOperationalAddress(
                payload.operationalAddress() != null
                        ? this.buildAddress(payload.operationalAddress())
                        : this.buildAddress(payload.legalAddress()));

        return this.clientsRepository.save(clientFromDB);
    }

    // void perché non c'è niente da restituire
    public void findByIdAndDelete(long clientId) {
        Client clientFromDB = this.findById(clientId);
        this.clientsRepository.delete(clientFromDB);
    }
}
