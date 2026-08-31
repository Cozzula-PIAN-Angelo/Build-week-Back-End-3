package com.epicode.buildweekbackend3.services;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.UnauthorizedException;
import com.epicode.buildweekbackend3.payloads.LoginDTO;
import com.epicode.buildweekbackend3.repositories.UsersRepository;
import com.epicode.buildweekbackend3.security.JWTTools;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsersRepository usersRepository;
    private final JWTTools jwtTools;
    private final PasswordEncoder bcrypt;

    public AuthService(UsersRepository usersRepository, JWTTools jwtTools, PasswordEncoder bcrypt) {
        this.usersRepository = usersRepository;
        this.jwtTools = jwtTools;
        this.bcrypt = bcrypt;
    }

    public String checkCredentialsAndGenerateToken(LoginDTO body) {
        User fromDB = this.usersRepository.findByEmail(body.email()).orElseThrow(() -> new UnauthorizedException("Credenziali Errate"));

        if (!bcrypt.matches(body.password(), fromDB.getPassword())) throw new UnauthorizedException("Credenziali Errate");

        return jwtTools.generateToken(fromDB);
    }
}