package com.epicode.buildweekbackend3.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Invoice extends BaseEntity {

    @NotBlank(message = "Il numero della fattura è obbligatorio.")
    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @NotNull(message = "La data della fattura è obbligatoria.")
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @NotNull(message = "L'importo è obbligatorio.")
    @Positive(message = "L'importo deve essere maggiore di zero.")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private InvoiceStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // createdAt/updatedAt e i callback @PrePersist/@PreUpdate arrivano da
    // BaseEntity: ridichiararli qui mapperebbe due campi sulla stessa colonna.

    public Invoice(String invoiceNumber, LocalDate invoiceDate, BigDecimal amount,
                   InvoiceStatus status, Client client) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.status = status;
        this.client = client;
    }
}
