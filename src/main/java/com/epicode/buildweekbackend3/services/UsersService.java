package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.Roles;
import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.ValidationException;
import com.epicode.buildweekbackend3.payloads.AssignRoleDTO;
import com.epicode.buildweekbackend3.payloads.NewUserDTO;
import com.epicode.buildweekbackend3.repositories.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder bcrypt;

    public UsersService(UsersRepository usersRepository, PasswordEncoder bcrypt) {
        this.usersRepository = usersRepository;
        this.bcrypt = bcrypt;
    }

    public List<User> findAll() { return this.usersRepository.findAll();}

    public User create(NewUserDTO payload) {
        if(this.usersRepository.findByEmail(payload.email()).isPresent())
            throw new ValidationException("L'email " + payload.email() + " e gia in uso");

        User newUser = new User(payload.email(), bcrypt.encode(payload.password()), payload.name(), payload.surname());
        return this.usersRepository.save(newUser);
    }
    public User findById(long userId) {
        return this.usersRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
    }


    public User findByIdAndUpdate(long userId, NewUserDTO payload) {
        User userFromDB = this.findById(userId);

        userFromDB.setName(payload.name());
        userFromDB.setSurname(payload.surname());
        userFromDB.setEmail(payload.email());
        userFromDB.setPassword(bcrypt.encode(payload.password()));

        return this.usersRepository.save(userFromDB);
    }
    public User findByIdAndUpdateRole(long userId, AssignRoleDTO body) {
        User userFromDB = this.findById(userId);
        try {
            userFromDB.setRole(Roles.valueOf(body.roles().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Ruolo non valido: " + body.roles() + ". Valori ammessi: USER,COMMERCIALE,CONTABILE,ADMIN");
        }
        return this.usersRepository.save(userFromDB);
    }

    public void findByIdAndDelete(long userId) {
        User userFromDB = this.findById(userId);
        this.usersRepository.delete(userFromDB);
    }
}