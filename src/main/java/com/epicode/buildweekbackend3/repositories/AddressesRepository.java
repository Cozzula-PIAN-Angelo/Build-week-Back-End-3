package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressesRepository extends JpaRepository<Address, Long> {
}