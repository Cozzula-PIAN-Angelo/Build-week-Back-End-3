package com.epicode.buildweekbackend3.payloads;

import com.epicode.buildweekbackend3.entities.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClientRespDTO(
        Long id,
        String companyName,
        String vatNumber,
        String email,
        BigDecimal annualRevenue,
        CompanyType companyType,
        String logoUrl,
        LocalDate lastContactDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        SalesRepDTO salesRep,
        AddressRespDTO legalAddress,
        AddressRespDTO operationalAddress
) {

    public record SalesRepDTO(long id, String name, String surname, String email, Roles role) {
    }

    public record AddressRespDTO(long id, String street, String buildingNumber, String city, String province, String postalCode) {
    }

    private static SalesRepDTO salesRepDto(User u) {
        return u == null ? null : new SalesRepDTO(u.getId(), u.getName(), u.getSurname(), u.getEmail(), u.getRole());
    }

    private static AddressRespDTO addressDto(Address a) {
        return a == null ? null : new AddressRespDTO(a.getId(), a.getStreet(), a.getBuildingNumber(), a.getCity(), a.getProvince(), a.getPostalCode());
    }

    public static ClientRespDTO from(Client c) {
        return new ClientRespDTO(
                c.getId(),
                c.getCompanyName(),
                c.getVatNumber(),
                c.getEmail(),
                c.getAnnualRevenue(),
                c.getCompanyType(),
                c.getLogoUrl(),
                c.getLastContactDate(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                salesRepDto(c.getSalesRep()),
                addressDto(c.getLegalAddress()),
                addressDto(c.getOperationalAddress())
        );
    }
}
