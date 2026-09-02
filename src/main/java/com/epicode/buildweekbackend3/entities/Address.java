package com.epicode.buildweekbackend3.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "addresses",
        indexes = {
                @Index(name = "idx_addresses_client_id", columnList = "client_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_addresses_client_type", columnNames = {"client_id", "address_type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @Column(nullable = false)
    private String street;

    @Column(name = "building_number", nullable = false)
    private String buildingNumber;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String province;

    @Column(name = "postal_code", nullable = false, length = 5)
    private String postalCode;

    // created_at/updated_at ora arrivano da BaseEntity (audit centralizzato).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;
}