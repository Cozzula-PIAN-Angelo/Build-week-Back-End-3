package com.epicode.buildweekbackend3.security;

import com.epicode.buildweekbackend3.entities.User;
import com.epicode.buildweekbackend3.exceptions.NotFoundException;
import com.epicode.buildweekbackend3.exceptions.UnauthorizedException;
import com.epicode.buildweekbackend3.services.UsersService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTTools jwtTools;
    private final UsersService usersService;

    public JWTFilter(JWTTools jwtTools, UsersService usersService) {
        this.jwtTools = jwtTools;
        this.usersService = usersService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            unauthorized(response, "Inserisci il token nell'header Authorization (Bearer ...)");
            return;
        }

        String accessToken = header.substring(7);

        try {
            jwtTools.verifyToken(accessToken);
            long currentUserId = jwtTools.extractIdFromToken(accessToken);
            User currentUser = this.usersService.findById(currentUserId);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UnauthorizedException ex) {
            unauthorized(response, ex.getMessage());
            return;
        } catch (NotFoundException ex) {
            unauthorized(response, "Utente del token non più esistente, rifai il login");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return new AntPathMatcher().match("/api/auth/**", request.getServletPath());
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"message\":\"" + message + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }
}
