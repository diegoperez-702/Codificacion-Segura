package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio de carga de usuarios para Spring Security.
 *
 * Mitigacion de "Exposicion de datos sensibles":
 * - El usuario y el hash de la contrasena provienen de configuracion
 *   externalizada, no estan hardcodeados en el codigo.
 * - La contrasena se almacena como hash BCrypt (no en texto plano ni {noop}).
 *   La comparacion la realiza el PasswordEncoder configurado en SecurityConfig.
 */
@Service
public class SecurityService implements UserDetailsService {

    private final String username;
    private final String passwordHash;

    public SecurityService(
            @Value("${app.auth.username}") String username,
            @Value("${app.auth.password-hash}") String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (this.username.equals(username)) {
            return new User(
                    this.username,
                    this.passwordHash,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        }
        // Mensaje generico: no se revela si el usuario existe o no.
        throw new UsernameNotFoundException("Credenciales invalidas");
    }
}
