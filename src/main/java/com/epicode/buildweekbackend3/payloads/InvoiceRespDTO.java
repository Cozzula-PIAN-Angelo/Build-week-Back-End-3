package com.epicode.buildweekbackend3.payloads;

import com.epicode.buildweekbackend3.entities.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceRespDTO(
        long id,
        String invoiceNumber,
        LocalDate invoiceDate,
        BigDecimal amount,
        long statusId,
        String statusName,
        long clientId,
        String clientCompanyName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InvoiceRespDTO from(Invoice invoice) {
        return new InvoiceRespDTO(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getAmount(),
                invoice.getStatus().getId(),
                invoice.getStatus().getName(),
                invoice.getClient().getId(),
                invoice.getClient().getCompanyName(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}