package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.ForbiddenException;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.NewClientDTO;
import com.epicode.buildweekbackend3.repositories.AddressesRepository;
import com.epicode.buildweekbackend3.repositories.ClientsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ClientsService {

    private final ClientsRepository clientsRepository; // per salvare/leggere clienti
    private final AddressesRepository addressesRepository; // per trasformare gli id degli indirizzi in oggetti Address

    public ClientsService(ClientsRepository clientsRepository, AddressesRepository addressesRepository) {
        this.clientsRepository = clientsRepository;
        this.addressesRepository = addressesRepository;
    }

    public Client create(NewClientDTO payload, User currentUser) {
        if (this.clientsRepository.existsByVatNumber(payload.vatNumber()))
            throw new ValidationException("La partita IVA " + payload.vatNumber() + " è già in uso");

        Client newClient = new Client(payload.companyName(), payload.vatNumber(), payload.companyType());
        newClient.setEmail(payload.email());
        newClient.setAnnualRevenue(payload.annualRevenue());
        newClient.setLastContactDate(payload.lastContactDate());

        Address legal = this.findAddress(payload.legalAddressId());
        newClient.setLegalAddress(legal);

        if (payload.operationalAddressId() != null) {
            Address operational = this.findAddress(payload.operationalAddressId());
            newClient.setOperationalAddress(operational);
        } else {
            newClient.setOperationalAddress(legal);
        }

        if (currentUser.getRole() == Roles.COMMERCIALE) {
            newClient.setSalesRep(currentUser);
        } else {
            newClient.setSalesRep(null);
        }

        return this.clientsRepository.save(newClient);
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

    // metodo che trasforma un id in un Address
    private Address findAddress(Long addressId) {
        return this.addressesRepository.findById(addressId).orElseThrow(() -> new NotFoundException(addressId));
    }

    public Client findByIdAndUpdate(long clientId, NewClientDTO payload, User currentUser) {
        Client clientFromDB = this.findById(clientId);

        this.checkCanManage(clientFromDB, currentUser);

        if (!clientFromDB.getVatNumber().equals(payload.vatNumber())
                && this.clientsRepository.existsByVatNumber(payload.vatNumber()))
            throw new ValidationException("La partita IVA " + payload.vatNumber() + " è già in uso");

        if (currentUser.getRole() != Roles.ADMIN && clientFromDB.getCompanyType() != payload.companyType()) {
            throw new ForbiddenException("Solo un ADMIN può modificare il tipo societario.");
        }

        clientFromDB.setCompanyName(payload.companyName());
        clientFromDB.setVatNumber(payload.vatNumber());
        clientFromDB.setEmail(payload.email());
        clientFromDB.setAnnualRevenue(payload.annualRevenue());
        clientFromDB.setCompanyType(payload.companyType());
        clientFromDB.setLastContactDate(payload.lastContactDate());

        Address legale = this.findAddress(payload.legalAddressId());
        clientFromDB.setLegalAddress(legale);
        clientFromDB.setOperationalAddress(
                payload.operationalAddressId() != null
                        ? this.findAddress(payload.operationalAddressId())
                        : legale);

        return this.clientsRepository.save(clientFromDB);
    }

    // void perché non c'è niente da restituire
    public void findByIdAndDelete(long clientId, User currentUser) {
        Client clientFromDB = this.findById(clientId);
        this.checkCanManage(clientFromDB, currentUser); // rete di sicurezza se un domani qualcuno allenta l'annotazione, e per coerenza con update.
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
}
