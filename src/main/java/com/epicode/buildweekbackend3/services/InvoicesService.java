package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Client;
import com.epicode.buildweekbackend3.entities.Invoice;
import com.epicode.buildweekbackend3.entities.InvoiceStatus;
import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.ForbiddenException;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.ChangeInvoiceStatusDTO;
import com.epicode.buildweekbackend3.payloads.InvoiceRespDTO;
import com.epicode.buildweekbackend3.payloads.NewInvoiceDTO;
import com.epicode.buildweekbackend3.repositories.ClientsRepository;
import com.epicode.buildweekbackend3.repositories.InvoiceStatusesRepository;
import com.epicode.buildweekbackend3.repositories.InvoicesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoicesService {

    private final InvoicesRepository invoicesRepository;
    private final ClientsRepository clientsRepository;
    private final InvoiceStatusesRepository invoiceStatusesRepository;
    private final InvoiceStatusesService invoiceStatusesService;

    public InvoicesService(InvoicesRepository invoicesRepository,
                           ClientsRepository clientsRepository,
                           InvoiceStatusesRepository invoiceStatusesRepository,
                           InvoiceStatusesService invoiceStatusesService) {
        this.invoicesRepository = invoicesRepository;
        this.clientsRepository = clientsRepository;
        this.invoiceStatusesRepository = invoiceStatusesRepository;
        this.invoiceStatusesService = invoiceStatusesService;
    }

    @Transactional(readOnly = true)
    public Page<InvoiceRespDTO> search(Long clientId, Long statusId,
                                       int page, int size, String sortBy) {
        if (size > 100) size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        return this.invoicesRepository
                .search(clientId, statusId, pageable)
                .map(InvoiceRespDTO::from);
    }

    @Transactional(readOnly = true)
    public InvoiceRespDTO findByIdAsDTO(long invoiceId) {
        return InvoiceRespDTO.from(this.findById(invoiceId));
    }

    public Invoice findById(long invoiceId) {
        return this.invoicesRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException(invoiceId));
    }

    @Transactional
    public InvoiceRespDTO create(NewInvoiceDTO payload) {
        if (this.invoicesRepository.existsByInvoiceNumber(payload.invoiceNumber()))
            throw new ValidationException("Il numero fattura " + payload.invoiceNumber() + " è già in uso");

        Client client = this.clientsRepository.findById(payload.clientId())
                .orElseThrow(() -> new NotFoundException(payload.clientId()));

        Invoice newInvoice = new Invoice(
                payload.invoiceNumber(),
                payload.invoiceDate(),
                payload.amount(),
                this.findInitialStatus(),
                client
        );

        return InvoiceRespDTO.from(this.invoicesRepository.save(newInvoice));
    }

    @Transactional
    public InvoiceRespDTO findByIdAndUpdate(long invoiceId, NewInvoiceDTO payload) {
        Invoice invoiceFromDB = this.findById(invoiceId);

        if (!invoiceFromDB.getInvoiceNumber().equals(payload.invoiceNumber())
                && this.invoicesRepository.existsByInvoiceNumber(payload.invoiceNumber()))
            throw new ValidationException("Il numero fattura " + payload.invoiceNumber() + " è già in uso");

        invoiceFromDB.setInvoiceNumber(payload.invoiceNumber());
        invoiceFromDB.setInvoiceDate(payload.invoiceDate());
        invoiceFromDB.setAmount(payload.amount());

        if (!invoiceFromDB.getClient().getId().equals(payload.clientId())) {
            Client newClient = this.clientsRepository.findById(payload.clientId())
                    .orElseThrow(() -> new NotFoundException(payload.clientId()));
            invoiceFromDB.setClient(newClient);
        }

        return InvoiceRespDTO.from(this.invoicesRepository.save(invoiceFromDB));
    }

    @Transactional
    public void findByIdAndDelete(long invoiceId) {
        Invoice invoiceFromDB = this.findById(invoiceId);
        this.invoicesRepository.delete(invoiceFromDB);
    }

    @Transactional
    public InvoiceRespDTO changeStatus(long invoiceId, ChangeInvoiceStatusDTO payload, User currentUser) {
        Invoice invoice = this.findById(invoiceId);
        InvoiceStatus currentStatus = invoice.getStatus();
        InvoiceStatus targetStatus = this.invoiceStatusesService.findById(payload.newStatusId());

        if (currentStatus.getId().equals(targetStatus.getId()))
            throw new ValidationException("La fattura è già nello stato " + targetStatus.getName());

        if (!currentStatus.canTransitionTo(targetStatus))
            throw new ValidationException("Transizione non consentita da " + currentStatus.getName()
                    + " a " + targetStatus.getName() + ". Stati raggiungibili: "
                    + this.listReachableStates(currentStatus));

        if (currentUser.getRole() != Roles.ADMIN && currentUser.getRole() != targetStatus.getRequiredRole())
            throw new ForbiddenException("Per portare una fattura nello stato " + targetStatus.getName()
                    + " serve il ruolo " + targetStatus.getRequiredRole());

        invoice.setStatus(targetStatus);
        return InvoiceRespDTO.from(this.invoicesRepository.save(invoice));
    }

    private InvoiceStatus findInitialStatus() {
        List<InvoiceStatus> initialStatuses = this.invoiceStatusesRepository.findInitialStatuses();

        if (initialStatuses.isEmpty())
            throw new ValidationException("Nessuno stato iniziale configurato: serve uno stato "
                    + "senza transizioni entranti (es. BOZZA)");

        if (initialStatuses.size() > 1)
            throw new ValidationException("Stato iniziale ambiguo: "
                    + initialStatuses.stream().map(InvoiceStatus::getName).collect(Collectors.joining(", "))
                    + ". Deve essercene uno solo");

        return initialStatuses.get(0);
    }

    private String listReachableStates(InvoiceStatus status) {
        if (status.getAllowedTransitions().isEmpty()) return "nessuno (stato terminale)";

        return status.getAllowedTransitions().stream()
                .map(InvoiceStatus::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}