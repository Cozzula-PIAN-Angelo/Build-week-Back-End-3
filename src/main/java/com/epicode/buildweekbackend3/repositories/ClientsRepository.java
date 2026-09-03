package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ClientsRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    boolean existsByVatNumber(String vatNumber);
    boolean existsByEmail(String email);
}
