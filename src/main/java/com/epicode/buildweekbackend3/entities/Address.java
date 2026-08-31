package com.epicode.buildweekbackend3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {
    @NotBlank
    private String street;      // via
    @NotBlank
    private String houseNumber; // civico
    @NotBlank
    private String locality;    // località
    @NotBlank
    private String zipCode;     // CAP
    @NotBlank
    private String municipality;// comune

    public Address(String street, String houseNumber, String locality, String zipCode, String municipality) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.locality = locality;
        this.zipCode = zipCode;
        this.municipality = municipality;
    }
}

