package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.NewClientDTO;
import com.epicode.buildweekbackend3.repositories.AddressesRepository;
import com.epicode.buildweekbackend3.repositories.ClientsRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientsService {

    private final ClientsRepository clientsRepository; // per salvare/leggere clienti
    private final AddressesRepository addressesRepository; // per trasformare gli id degli indirizzi in oggetti Address

    public ClientsService(ClientsRepository clientsRepository, AddressesRepository addressesRepository) {
        this.clientsRepository = clientsRepository;
        this.addressesRepository = addressesRepository;
    }

    public Client create(NewClientDTO payload) {
        if (this.clientsRepository.existsByVatNumber(payload.vatNumber()))
            throw new ValidationException("La partita IVA " + payload.vatNumber() + " è già in uso");

        Client newClient = new Client(payload.companyName(), payload.vatNumber(), payload.companyType());
        newClient.setEmail(payload.email());
        newClient.setAnnualRevenue(payload.annualRevenue());

        Address legale = this.addressesRepository.findById(payload.legalAddressId())
                .orElseThrow(() -> new NotFoundException(payload.legalAddressId()));
        newClient.setLegalAddress(legale);

        if (payload.operationalAddressId() != null) {
            Address operativo = this.addressesRepository.findById(payload.operationalAddressId())
                    .orElseThrow(() -> new NotFoundException(payload.operationalAddressId()));
            newClient.setOperationalAddress(operativo);
        } else {
            newClient.setOperationalAddress(legale);
        }

        return this.clientsRepository.save(newClient);
    }

    // findById ritorna un Optional<Client> (potrebbe esserci o no). orElseThrow significa "dammi il cliente, oppure lancia NotFoundException" → HTTP 404. Identico a
    //  UsersService.findById().
    public Client findById(long clientId) {
        return this.clientsRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException(clientId));
    }
}
