package com.epicode.buildweekbackend3.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "clients", indexes = {
        @Index(name = "idx_clients_sales_rep_id", columnList = "sales_rep_id")
})
public class Client extends BaseEntity {

    @NotBlank(message = "Il nome dell'azienda è obbligatorio.")
    @Column(nullable = false)
    private String companyName;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "Il numero di partita IVA deve avere 11 caratteri.")
    @Column(nullable = false, unique = true, length = 11)
    private String vatNumber;

    @Email(message = "E-mail non valida.")
    @Column(unique = true)
    private String email;

    @PositiveOrZero
    @Column(precision = 15, scale = 2)
    private BigDecimal annualRevenue;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType companyType;

    private String logoUrl;

    // niente @Column(nullable = false) perché deve restare opzionale
    private LocalDate lastContactDate;

    @ManyToOne(fetch = FetchType.LAZY) // annotazione che definisce una relazione tra due entità nel database
    @JoinColumn(name = "sales_rep_id")
    private User salesRep;

    // Address non esiste senza un Client: è Address a possedere la FK
    // (client_id, address_type), non più Client. Un cliente ha al massimo
    // una sede LEGAL e una OPERATIONAL (vincolo UNIQUE su Address).
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();

    public Client(String companyName, String vatNumber, CompanyType companyType) {
        this.companyName = companyName;
        this.vatNumber = vatNumber;
        this.companyType = companyType;
    }

    // Metodi di comodo: il codice chiamante (ClientsService) continua a
    // leggere/scrivere legalAddress/operationalAddress come prima
    // dell'inversione della relazione, senza maneggiare direttamente la
    // collection.
    public Address getLegalAddress() {
        return findAddressByType(AddressType.LEGAL);
    }

    public Address getOperationalAddress() {
        return findAddressByType(AddressType.OPERATIONAL);
    }

    public void setLegalAddress(Address address) {
        replaceAddress(AddressType.LEGAL, address);
    }

    public void setOperationalAddress(Address address) {
        replaceAddress(AddressType.OPERATIONAL, address);
    }

    private Address findAddressByType(AddressType type) {
        return this.addresses.stream()
                .filter(a -> a.getAddressType() == type)
                .findFirst()
                .orElse(null);
    }

    // Se un indirizzo di quel tipo esiste già, ne aggiorna i campi in place
    // invece di sostituirlo con una riga nuova: creare un Address nuovo e
    // rimuovere il vecchio nella stessa flush violerebbe temporaneamente
    // uk_addresses_client_type (Hibernate esegue gli INSERT prima delle
    // DELETE da orphanRemoval). Se non esiste ancora, lo aggiunge.
    private void replaceAddress(AddressType type, Address newAddress) {
        if (newAddress == null) {
            this.addresses.removeIf(a -> a.getAddressType() == type);
            return;
        }

        Address existing = findAddressByType(type);
        if (existing != null) {
            existing.setStreet(newAddress.getStreet());
            existing.setBuildingNumber(newAddress.getBuildingNumber());
            existing.setCity(newAddress.getCity());
            existing.setProvince(newAddress.getProvince());
            existing.setPostalCode(newAddress.getPostalCode());
        } else {
            newAddress.setAddressType(type);
            newAddress.setClient(this);
            this.addresses.add(newAddress);
        }
    }
}