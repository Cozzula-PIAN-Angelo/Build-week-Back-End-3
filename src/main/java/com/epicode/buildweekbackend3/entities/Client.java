package com.epicode.buildweekbackend3.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "clients")
public class Client extends BaseEntity {

    @NotBlank(message = "Il nome dell'azienda è obbligatorio.")
    @Column(nullable = false)
    private String companyName;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "Il numero di partita IVA deve avere 11 caratteri.")
    @Column(nullable = false, unique = true)
    private String vatNumber;

    @Email(message = "E-mail non valida.")
    private String email;

    @PositiveOrZero
    private BigDecimal annualRevenue;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType companyType;

    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY) // annotazione che definisce una relazione tra due entità nel database
    @JoinColumn(name = "sales_rep_id")
    private User salesRep;

    @ManyToOne(fetch = FetchType.LAZY) // Carica legalAddress solo se/quando lo usiamo davvero
    @JoinColumn(name = "legal_address_id")
    private Address legalAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operational_address_id")
    private Address operationalAddress;

    public Client(String companyName, String vatNumber, CompanyType companyType) {
        this.companyName = companyName;
        this.vatNumber = vatNumber;
        this.companyType = companyType;
    }
}