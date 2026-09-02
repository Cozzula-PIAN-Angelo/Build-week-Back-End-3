package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvoiceStatusesRepository extends JpaRepository<InvoiceStatus, Long> {

    Optional<InvoiceStatus> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT s FROM InvoiceStatus s WHERE s.id NOT IN " +
            "(SELECT target.id FROM InvoiceStatus source JOIN source.allowedTransitions target)")
    List<InvoiceStatus> findInitialStatuses();
}