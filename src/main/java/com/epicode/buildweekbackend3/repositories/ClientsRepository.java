package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ClientsRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    Optional<Client> findByVatNumber(String vatNumber);
    boolean existsByVatNumber(String vatNumber);
}
