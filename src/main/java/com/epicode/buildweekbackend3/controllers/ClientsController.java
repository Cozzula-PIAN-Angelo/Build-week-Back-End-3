package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.payloads.NewClientDTO;
import com.epicode.buildweekbackend3.services.ClientsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientsController {
    private final ClientsService clientsService;

    public ClientsController(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'COMMERCIALE', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Client create(@RequestBody @Valid NewClientDTO payload,
                         @AuthenticationPrincipal User currentUser) {
        return this.clientsService.create(payload, currentUser);
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

    @PutMapping("/{clientId}")
    @PreAuthorize("hasAnyRole('COMMERCIALE', 'ADMIN')")
    public Client update(@PathVariable long clientId, @RequestBody @Valid NewClientDTO payload, @AuthenticationPrincipal User currentUser) {
        return this.clientsService.findByIdAndUpdate(clientId, payload,  currentUser);
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long clientId, @AuthenticationPrincipal User currentUser) {
        this.clientsService.findByIdAndDelete(clientId, currentUser);
    }
}
