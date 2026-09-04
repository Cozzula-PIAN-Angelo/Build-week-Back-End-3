package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.payloads.ChangeInvoiceStatusDTO;
import com.epicode.buildweekbackend3.payloads.InvoiceRespDTO;
import com.epicode.buildweekbackend3.payloads.NewInvoiceDTO;
import com.epicode.buildweekbackend3.services.InvoicesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoicesController {

    private final InvoicesService invoicesService;

    public InvoicesController(InvoicesService invoicesService) {
        this.invoicesService = invoicesService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER', 'COMMERCIALE', 'CONTABILE', 'ADMIN')")
    public Page<InvoiceRespDTO> getAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return this.invoicesService.search(clientId, statusId, page, size, sortBy);
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAnyAuthority('USER', 'COMMERCIALE', 'CONTABILE', 'ADMIN')")
    public InvoiceRespDTO getById(@PathVariable long invoiceId) {
        return this.invoicesService.findByIdAsDTO(invoiceId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CONTABILE', 'ADMIN')")
    public InvoiceRespDTO create(@RequestBody @Valid NewInvoiceDTO payload) {
        return this.invoicesService.create(payload);
    }

    @PutMapping("/{invoiceId}")
    @PreAuthorize("hasAnyAuthority('CONTABILE', 'ADMIN')")
    public InvoiceRespDTO update(@PathVariable long invoiceId, @RequestBody @Valid NewInvoiceDTO payload) {
        return this.invoicesService.findByIdAndUpdate(invoiceId, payload);
    }

    @PatchMapping("/{invoiceId}/status")
    @PreAuthorize("hasAnyAuthority('CONTABILE', 'ADMIN')")
    public InvoiceRespDTO changeStatus(@PathVariable long invoiceId,
                                       @RequestBody @Valid ChangeInvoiceStatusDTO payload,
                                       @AuthenticationPrincipal User currentUser) {
        return this.invoicesService.changeStatus(invoiceId, payload, currentUser);
    }

    @DeleteMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('CONTABILE', 'ADMIN')")
    public void delete(@PathVariable long invoiceId) {
        this.invoicesService.findByIdAndDelete(invoiceId);
    }
}