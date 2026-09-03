package com.epicode.buildweekbackend3.config;

import com.epicode.buildweekbackend3.entities.InvoiceStatus;
import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.repositories.InvoiceStatusesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Popola gli stati fattura al primo avvio su un database vuoto.
//
// Serve perche' il motore delle transizioni legge il ciclo di vita dai dati:
// senza queste righe una POST /api/invoices fallisce con "Nessuno stato
// iniziale configurato". La traccia pretende un ordine preciso e INSOLUTA
// riservata all'ADMIN: qui quell'ordine viene garantito, non solo reso
// possibile.
//
// Idempotente: se esiste gia' almeno uno stato non tocca niente. Cosi' un
// riavvio non duplica le righe e non sovrascrive un grafo che il CONTABILE
// avesse modificato via API (che resta il modo previsto per cambiarlo).
@Component
public class InvoiceStatusSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusSeeder.class);

    private final InvoiceStatusesRepository invoiceStatusesRepository;

    public InvoiceStatusSeeder(InvoiceStatusesRepository invoiceStatusesRepository) {
        this.invoiceStatusesRepository = invoiceStatusesRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (this.invoiceStatusesRepository.count() > 0) {
            log.info("Stati fattura gia' presenti ({}): seed saltato",
                    this.invoiceStatusesRepository.count());
            return;
        }

        log.info("Database senza stati fattura: creo il ciclo di vita iniziale");

        // 1. Gli stati. BOZZA non compare mai come destinazione: e' questo che
        // ne fa lo stato iniziale, riconosciuto da findInitialStatuses().
        InvoiceStatus bozza = this.createStatus("BOZZA",
                "Fattura creata, non ancora emessa", Roles.CONTABILE);
        InvoiceStatus emessa = this.createStatus("EMESSA",
                "Fattura emessa e inviata al cliente", Roles.CONTABILE);
        InvoiceStatus pagata = this.createStatus("PAGATA",
                "Pagamento incassato (stato terminale)", Roles.CONTABILE);
        InvoiceStatus scaduta = this.createStatus("SCADUTA",
                "Termine di pagamento superato", Roles.CONTABILE);
        // Unico stato riservato all'ADMIN, come chiede la traccia.
        InvoiceStatus insoluta = this.createStatus("INSOLUTA",
                "Credito non recuperabile", Roles.ADMIN);

        // 2. Le transizioni ammesse:
        //    BOZZA -> EMESSA -> PAGATA
        //                    -> SCADUTA -> INSOLUTA
        //
        // PAGATA e INSOLUTA non hanno transizioni uscenti: sono terminali.
        // Nessuna coppia inversa e' presente, ed e' cosi' che il grafo
        // impedisce di tornare a uno stato precedente.
        this.allowTransition(bozza, emessa);
        this.allowTransition(emessa, pagata);
        this.allowTransition(emessa, scaduta);
        this.allowTransition(scaduta, insoluta);

        log.info("Creati 5 stati fattura e 4 transizioni");
    }

    private InvoiceStatus createStatus(String name, String description, Roles requiredRole) {
        InvoiceStatus status = new InvoiceStatus(name, requiredRole);
        status.setDescription(description);
        return this.invoiceStatusesRepository.save(status);
    }

    private void allowTransition(InvoiceStatus from, InvoiceStatus to) {
        from.getAllowedTransitions().add(to);
        this.invoiceStatusesRepository.save(from);
    }
}