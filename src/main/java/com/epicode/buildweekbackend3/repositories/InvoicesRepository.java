package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InvoicesRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);
    boolean existsByStatusId(long statusId);
    boolean existsByClientId(long clientId);

    @Query("""
            SELECT i FROM Invoice i
            WHERE (:clientId IS NULL OR i.client.id = :clientId)
              AND (:statusId IS NULL OR i.status.id = :statusId)
              AND (:minAmount IS NULL OR i.amount >= :minAmount)
              AND (:maxAmount IS NULL OR i.amount <= :maxAmount)
              AND (:fromDate IS NULL OR i.invoiceDate >= :fromDate)
              AND (:toDate IS NULL OR i.invoiceDate <= :toDate)
            """)
    Page<Invoice> search(@Param("clientId") Long clientId,
                         @Param("statusId") Long statusId,
                         @Param("minAmount") BigDecimal minAmount,
                         @Param("maxAmount") BigDecimal maxAmount,
                         @Param("fromDate") LocalDate fromDate,
                         @Param("toDate") LocalDate toDate,
                         Pageable pageable);
}