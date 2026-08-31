package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.payloads.AssignRoleDTO;
import com.epicode.buildweekbackend3.payloads.NewUserDTO;
import com.epicode.buildweekbackend3.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @GetMapping
    public List<User> getAll() {
        return this.usersService.findAll();
    }

    @GetMapping("/me")
    public User getProfile(@AuthenticationPrincipal User currentUser) {
        return currentUser;
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable long userId) {
        return this.usersService.findById(userId);
    }

    @PutMapping("/me")
    public User updateProfile(@AuthenticationPrincipal User currentUser, @RequestBody @Valid NewUserDTO payload) {
        return this.usersService.findByIdAndUpdate(currentUser.getId(), payload);
    }

    @PutMapping("/{userId}")
    public User update(@PathVariable long userId, @RequestBody @Valid NewUserDTO payload) {
        return this.usersService.findByIdAndUpdate(userId, payload);
    }

    @PatchMapping("/{userId}/role")
    public User assignRole(@PathVariable long userId, @RequestBody @Valid AssignRoleDTO body) {
        return this.usersService.findByIdAndUpdateRole(userId, body);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal User currentUser) {
        this.usersService.findByIdAndDelete(currentUser.getId());
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId) {
        this.usersService.findByIdAndDelete(userId);
    }
}
