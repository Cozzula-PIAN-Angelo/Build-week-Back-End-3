package com.epicode.buildweekbackend3.repositories;
import com.epicode.buildweekbackend3.entities.User;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;


public interface UsersRepository extends Repository<User, Long> {
    Optional<User> findById(Long id);
    List<User> findByName(String name);
}
