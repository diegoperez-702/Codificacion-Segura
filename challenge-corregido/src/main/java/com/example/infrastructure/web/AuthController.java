package com.example.infrastructure.web;

import com.example.domain.model.LoginRequest;
import com.example.infrastructure.security.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de autenticacion. Valida credenciales y, si son correctas, emite
 * un JWT firmado.
 *
 * Notas de seguridad:
 * - Las credenciales se verifican con el AuthenticationManager de Spring, que
 *   usa el PasswordEncoder (BCrypt) configurado.
 * - Ante credenciales invalidas se devuelve 401 con un mensaje generico, sin
 *   revelar si el usuario existe (evita enumeracion de usuarios).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            String token = jwtTokenUtil.generateToken(authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "tokenType", "Bearer",
                    "accessToken", token));
        } catch (BadCredentialsException | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // Mensaje generico y uniforme: no distingue usuario inexistente de password erroneo.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales invalidas"));
        }
    }
}
