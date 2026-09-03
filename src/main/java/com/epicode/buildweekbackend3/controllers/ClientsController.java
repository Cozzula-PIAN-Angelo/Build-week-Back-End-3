package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.payloads.AssignSalesRepDTO;
import com.epicode.buildweekbackend3.payloads.ClientFilterDTO;
import com.epicode.buildweekbackend3.payloads.ClientRespDTO;
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
    public ClientRespDTO create(@RequestBody @Valid NewClientDTO payload,
                                @AuthenticationPrincipal User currentUser) {
        return ClientRespDTO.from(this.clientsService.create(payload, currentUser));
    }

    @GetMapping("/{clientId}")
    public ClientRespDTO getById(@PathVariable long clientId) {
        return ClientRespDTO.from(this.clientsService.findById(clientId));
    }

    @GetMapping
    public Page<ClientRespDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "id") String sortBy, ClientFilterDTO filter) {
        return this.clientsService.findAll(page, size, sortBy, filter).map(ClientRespDTO::from);
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasAnyRole('COMMERCIALE', 'ADMIN')")
    public ClientRespDTO update(@PathVariable long clientId, @RequestBody @Valid NewClientDTO payload, @AuthenticationPrincipal User currentUser) {
        return ClientRespDTO.from(this.clientsService.findByIdAndUpdate(clientId, payload, currentUser));
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long clientId, @AuthenticationPrincipal User currentUser) {
        this.clientsService.findByIdAndDelete(clientId, currentUser);
    }

    @PatchMapping("/{clientId}/sales-rep")
    @PreAuthorize("hasRole('ADMIN')")
    public ClientRespDTO assignSalesRep(@PathVariable long clientId, @RequestBody @Valid AssignSalesRepDTO body) {
        return ClientRespDTO.from(this.clientsService.assignSalesRep(clientId, body.salesRepId()));
    }
}
