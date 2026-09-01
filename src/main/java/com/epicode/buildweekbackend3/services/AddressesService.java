package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.payloads.AddressDTO;
import com.epicode.buildweekbackend3.repositories.AddressesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddressesService {

    @Autowired
    private AddressesRepository addressesRepository;

    public Page<Address> findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return addressesRepository.findAll(pageable);
    }

    public Address findById(UUID id) {
        return addressesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Indirizzo con id " + id + " non trovato!"));
    }

    public Address save(AddressDTO body) {
        Address newAddress = Address.builder()
                .street(body.street())
                .buildingNumber(body.buildingNumber())
                .city(body.city())
                .province(body.province().toUpperCase())
                .postalCode(body.postalCode())
                .build();

        return addressesRepository.save(newAddress);
    }

    public Address findByIdAndUpdate(UUID id, AddressDTO body) {
        Address found = this.findById(id);

        found.setStreet(body.street());
        found.setBuildingNumber(body.buildingNumber());
        found.setCity(body.city());
        found.setProvince(body.province().toUpperCase());
        found.setPostalCode(body.postalCode());

        return addressesRepository.save(found);
    }

    public void findByIdAndDelete(UUID id) {
        Address found = this.findById(id);
        try {
            addressesRepository.delete(found);
            addressesRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Impossibile eliminare l'indirizzo: risulta in uso come sede legale o operativa di un cliente.");
        }
    }
}