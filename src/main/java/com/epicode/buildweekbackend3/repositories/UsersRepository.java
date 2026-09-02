package com.epicode.buildweekbackend3.repositories;
import com.epicode.buildweekbackend3.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;


public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findBySurname(String surname);
}
