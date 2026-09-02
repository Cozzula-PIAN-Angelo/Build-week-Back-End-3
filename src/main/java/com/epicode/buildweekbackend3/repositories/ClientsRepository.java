package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientsRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByVatNumber(String vatNumber);
    boolean existsByVatNumber(String vatNumber);
    boolean existsByEmail(String email);
}
