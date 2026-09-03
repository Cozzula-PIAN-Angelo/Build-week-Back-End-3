package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.repositories.AddressesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

// Sola lettura: la modifica dei campi di un indirizzo passa dal PUT sul
// Cliente (ClientsService.findByIdAndUpdate), che è anche l'unico punto in
// cui si applica il controllo di competenza sul referente (checkCanManage).
@Service
public class AddressesService {

    @Autowired
    private AddressesRepository addressesRepository;

    public Page<Address> findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return addressesRepository.findAll(pageable);
    }

    public Address findById(Long id) {
        return addressesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Indirizzo con id " + id + " non trovato!"));
    }
}
