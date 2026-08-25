package com.example.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String subject) {
        return Jwts.builder()
               .setSubject(subject)
               .setIssuedAt(new Date())
               .setExpiration(new Date(System.currentTimeMillis() + 3600000))
               .signWith(key, SignatureAlgorithm.HS256)
               .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody();
    }
}