package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.services.AddressesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

// Un Address non esiste senza un Client (client_id NOT NULL) e non ha una
// vita propria: creazione, modifica ed eliminazione degli indirizzi
// passano SEMPRE da ClientsController (POST/PUT /api/clients), che
// crea/sostituisce legalAddress e operationalAddress insieme al Client.
// Questa è la "strada 1" richiesta dalla consegna: con gli indirizzi
// annidati nel Cliente non ha senso un endpoint di modifica dedicato,
// quindi qui restano solo operazioni di lettura.
@RestController
@RequestMapping("/api/addresses")
public class AddressesController {

    @Autowired
    private AddressesService addressesService;

    @GetMapping
    public Page<Address> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return addressesService.findAll(page, size, sortBy);
    }

    @GetMapping("/{id}")
    public Address getById(@PathVariable Long id) {
        return addressesService.findById(id);
    }
}
