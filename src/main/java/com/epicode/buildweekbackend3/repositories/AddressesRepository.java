package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressesRepository extends JpaRepository<Address, Long> {
}
