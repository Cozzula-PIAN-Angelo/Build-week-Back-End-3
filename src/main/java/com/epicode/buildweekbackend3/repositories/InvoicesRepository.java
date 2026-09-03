package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoicesRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);
    boolean existsByStatusId(long statusId);
    boolean existsByClientId(long clientId);

    @Query("""
            SELECT i FROM Invoice i
            WHERE (:clientId IS NULL OR i.client.id = :clientId)
              AND (:statusId IS NULL OR i.status.id = :statusId)
            """)
    Page<Invoice> search(@Param("clientId") Long clientId,
                         @Param("statusId") Long statusId,
                         Pageable pageable);
}