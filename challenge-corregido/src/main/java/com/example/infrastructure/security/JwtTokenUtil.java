package com.example.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad de generacion y validacion de tokens JWT.
 *
 * Decisiones de seguridad:
 * - La clave secreta se inyecta desde configuracion externalizada
 *   ({@code app.jwt.secret}), NO se genera en cada arranque. Generarla por
 *   arranque invalidaba todos los tokens al reiniciar e impedia escalar a
 *   varias instancias. Externalizarla tambien evita hardcodear secretos en el
 *   codigo (mitiga exposicion de datos sensibles).
 * - Se firma con HS256 y una clave de longitud suficiente (>= 256 bits).
 * - La validacion (parseToken) lanza excepcion ante firma invalida o token
 *   expirado, y expone metodos seguros para verificar validez.
 */
@Component
public class JwtTokenUtil {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtTokenUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMillis) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 exige al menos 256 bits (32 bytes). Fallar rapido si el secreto es debil.
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 bytes (256 bits) para HS256");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
    }

    /** Genera un token firmado para el subject (nombre de usuario) indicado. */
    public String generateToken(String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parsea y valida el token. Lanza {@link JwtException} si la firma es
     * invalida, esta manipulado o expiro.
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Devuelve true si el token es valido (firma correcta y no expirado). */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Extrae el subject (username) del token validado. */
    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }
}
