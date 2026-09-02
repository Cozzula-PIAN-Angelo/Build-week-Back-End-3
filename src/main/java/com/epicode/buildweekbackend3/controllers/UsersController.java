package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.payloads.AssignRoleDTO;
import com.epicode.buildweekbackend3.payloads.NewUserDTO;
import com.epicode.buildweekbackend3.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    // Solo ADMIN può visualizzare tutti gli utenti
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAll() {
        return this.usersService.findAll();
    }

    // Ogni utente autenticato può visualizzare il proprio profilo
    @GetMapping("/me")
    public User getProfile(@AuthenticationPrincipal User currentUser) {
        return currentUser;
    }

    // Solo ADMIN può visualizzare un altro utente
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public User getById(@PathVariable long userId) {
        return this.usersService.findById(userId);
    }

    // Solo ADMIN può modificare i dati di un utente
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public User update(
            @PathVariable long userId,
            @RequestBody @Valid NewUserDTO payload) {

        return this.usersService.findByIdAndUpdate(userId, payload);
    }

    // Solo ADMIN può modificare il ruolo di un altro utente
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public User assignRole(
            @PathVariable long userId,
            @RequestBody @Valid AssignRoleDTO body) {

        return this.usersService.findByIdAndUpdateRole(userId, body);
    }

    // Solo ADMIN può eliminare un utente
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable long userId) {
        this.usersService.findByIdAndDelete(userId);
    }
}
