package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.payloads.NewClientDTO;
import com.epicode.buildweekbackend3.services.ClientsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientsController {
    private final ClientsService clientsService;

    public ClientsController(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Client create(@RequestBody @Valid NewClientDTO payload) {
        return this.clientsService.create(payload);
    }

    @GetMapping("/{clientId}")
    public Client getById(@PathVariable long clientId) {
        return this.clientsService.findById(clientId);
    }

    @GetMapping
    public Page<Client> getAll(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "id") String sortBy) {
        return this.clientsService.findAll(page, size, sortBy);
    }
}
