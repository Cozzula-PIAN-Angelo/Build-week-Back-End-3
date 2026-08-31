package com.epicode.buildweekbackend3.security;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JWTTools {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 giorni
                .signWith(key())
                .compact();
    }

    public void verifyToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
        } catch (Exception ex) {
            throw new UnauthorizedException("Token non valido o scaduto, rifai il login");
        }
    }

    public long extractIdFromToken(String token) {
        return Long.parseLong(
                Jwts.parser().verifyWith(key()).build()
                        .parseSignedClaims(token).getPayload().getSubject());
    }
}
