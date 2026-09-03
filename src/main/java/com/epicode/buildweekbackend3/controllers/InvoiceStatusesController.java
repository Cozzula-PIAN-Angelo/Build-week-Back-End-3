package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.payloads.InvoiceStatusRespDTO;
import com.epicode.buildweekbackend3.payloads.NewInvoiceStatusDTO;
import com.epicode.buildweekbackend3.services.InvoiceStatusesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-statuses")
public class InvoiceStatusesController {

    private final InvoiceStatusesService invoiceStatusesService;

    public InvoiceStatusesController(InvoiceStatusesService invoiceStatusesService) {
        this.invoiceStatusesService = invoiceStatusesService;
    }

    @GetMapping
    public List<InvoiceStatusRespDTO> getAll() {
        return this.invoiceStatusesService.findAll();
    }

    @GetMapping("/{statusId}")
    public InvoiceStatusRespDTO getById(@PathVariable long statusId) {
        return this.invoiceStatusesService.findByIdAsDTO(statusId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CONTABILE', 'ADMIN')")
    public InvoiceStatusRespDTO create(@RequestBody @Valid NewInvoiceStatusDTO payload) {
        return this.invoiceStatusesService.create(payload);
    }

    @PutMapping("/{statusId}")
    @PreAuthorize("hasAnyAuthority('CONTABILE', 'ADMIN')")
    public InvoiceStatusRespDTO update(@PathVariable long statusId,
                                       @RequestBody @Valid NewInvoiceStatusDTO payload) {
        return this.invoiceStatusesService.findByIdAndUpdate(statusId, payload);
    }

    @DeleteMapping("/{statusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('CONTABILE', 'ADMIN')")
    public void delete(@PathVariable long statusId) {
        this.invoiceStatusesService.findByIdAndDelete(statusId);
    }
}