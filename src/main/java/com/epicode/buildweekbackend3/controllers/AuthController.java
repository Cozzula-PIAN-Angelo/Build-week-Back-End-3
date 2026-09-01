package com.epicode.buildweekbackend3.controllers;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.payloads.LoginDTO;
import com.epicode.buildweekbackend3.payloads.LoginRespDTO;
import com.epicode.buildweekbackend3.payloads.NewUserDTO;
import com.epicode.buildweekbackend3.payloads.NewUserRespDTO;
import com.epicode.buildweekbackend3.services.AuthService;
import com.epicode.buildweekbackend3.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsersService usersService;

    public AuthController(AuthService authService, UsersService usersService) {
        this.authService = authService;
        this.usersService = usersService;
    }

    @PostMapping("/login")
    public LoginRespDTO login(@RequestBody @Valid LoginDTO body) {
        String accessToken = this.authService.checkCredentialsAndGenerateToken(body);
        return new LoginRespDTO(accessToken);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public NewUserRespDTO register(@RequestBody @Valid NewUserDTO payload) {
        User utenteCreato = this.usersService.create(payload);
        return new NewUserRespDTO(utenteCreato.getId());
    }
}
