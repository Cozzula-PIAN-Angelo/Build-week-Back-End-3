package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.InvoiceStatus;
import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.InvoiceStatusRespDTO;
import com.epicode.buildweekbackend3.payloads.NewInvoiceStatusDTO;
import com.epicode.buildweekbackend3.repositories.InvoiceStatusesRepository;
import com.epicode.buildweekbackend3.repositories.InvoicesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class InvoiceStatusesService {

    private final InvoiceStatusesRepository invoiceStatusesRepository;
    private final InvoicesRepository invoicesRepository; // solo per il vincolo di cancellazione

    public InvoiceStatusesService(InvoiceStatusesRepository invoiceStatusesRepository,
                                  InvoicesRepository invoicesRepository) {
        this.invoiceStatusesRepository = invoiceStatusesRepository;
        this.invoicesRepository = invoicesRepository;
    }

    @Transactional(readOnly = true)
    public List<InvoiceStatusRespDTO> findAll() {
        return this.invoiceStatusesRepository.findAll().stream()
                .map(InvoiceStatusRespDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceStatusRespDTO findByIdAsDTO(long statusId) {
        return InvoiceStatusRespDTO.from(this.findById(statusId));
    }

    public InvoiceStatus findById(long statusId) {
        return this.invoiceStatusesRepository.findById(statusId)
                .orElseThrow(() -> new NotFoundException(statusId));
    }

    @Transactional
    public InvoiceStatusRespDTO create(NewInvoiceStatusDTO payload) {
        if (this.invoiceStatusesRepository.existsByName(payload.name()))
            throw new ValidationException("Esiste già uno stato con nome " + payload.name());

        InvoiceStatus newStatus = new InvoiceStatus();
        newStatus.setName(payload.name());
        newStatus.setDescription(payload.description());
        newStatus.setRequiredRole(payload.requiredRole() != null ? payload.requiredRole() : Roles.CONTABILE);
        newStatus.setAllowedTransitions(this.resolveTransitions(payload.allowedTransitionIds(), null));

        return InvoiceStatusRespDTO.from(this.invoiceStatusesRepository.save(newStatus));
    }

    @Transactional
    public InvoiceStatusRespDTO findByIdAndUpdate(long statusId, NewInvoiceStatusDTO payload) {
        InvoiceStatus statusFromDB = this.findById(statusId);

        if (!statusFromDB.getName().equals(payload.name())
                && this.invoiceStatusesRepository.existsByName(payload.name()))
            throw new ValidationException("Esiste già uno stato con nome " + payload.name());

        statusFromDB.setName(payload.name());
        statusFromDB.setDescription(payload.description());
        if (payload.requiredRole() != null) statusFromDB.setRequiredRole(payload.requiredRole());
        statusFromDB.setAllowedTransitions(this.resolveTransitions(payload.allowedTransitionIds(), statusId));

        return InvoiceStatusRespDTO.from(this.invoiceStatusesRepository.save(statusFromDB));
    }

    @Transactional
    public void findByIdAndDelete(long statusId) {
        InvoiceStatus statusFromDB = this.findById(statusId);
        if (this.invoicesRepository.existsByStatusId(statusId))
            throw new ValidationException("Lo stato " + statusFromDB.getName()
                    + " non è eliminabile: è lo stato corrente di almeno una fattura");
        this.invoiceStatusesRepository.findAll()
                .forEach(other -> other.getAllowedTransitions().remove(statusFromDB));

        this.invoiceStatusesRepository.flush(); // Il flush() esplicito prima della delete risolve il rischio
        // di violazione FK sul lato to_status_id: le DELETE sulla join-table degli altri stati vengono eseguite subito,
        // prima della cancellazione dell'entità.

        this.invoiceStatusesRepository.delete(statusFromDB);
    }

    private Set<InvoiceStatus> resolveTransitions(Set<Long> transitionIds, Long selfId) {
        Set<InvoiceStatus> transitions = new HashSet<>();
        if (transitionIds == null) return transitions;

        transitionIds.forEach(targetId -> {
            if (selfId != null && selfId.equals(targetId))
                throw new ValidationException("Uno stato non può transitare verso se stesso");
            transitions.add(this.findById(targetId));
        });

        return transitions;
    }
}