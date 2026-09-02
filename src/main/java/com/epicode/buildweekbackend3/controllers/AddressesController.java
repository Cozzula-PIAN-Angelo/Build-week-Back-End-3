package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.Address;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.AddressDTO;
import com.epicode.buildweekbackend3.services.AddressesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

// Un Address non esiste senza un Client (client_id NOT NULL): la creazione
// e l'eliminazione passano quindi da ClientsController (che crea/sostituisce
// legalAddress/operationalAddress insieme al Client), non da qui. Questo
// controller resta di sola lettura più un update dei campi (via, città,
// ecc.) di un indirizzo già legato a un cliente.
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COMMERCIALE')")
    public Address update(@PathVariable Long id, @RequestBody @Validated AddressDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            String messages = validation.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException(messages);
        }
        return addressesService.findByIdAndUpdate(id, body);
    }
}