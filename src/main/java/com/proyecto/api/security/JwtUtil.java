package com.proyecto.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generarToken(int idUsuario, String nombre, String rol) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(nombre)
                .claim("idUsuario", idUsuario)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            parsearToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] Token invalido: {}", e.getMessage());
            return false;
        }
    }

    public String extraerNombre(String token) {
        return parsearToken(token).getPayload().getSubject();
    }

    public int extraerIdUsuario(String token) {
        return parsearToken(token).getPayload().get("idUsuario", Integer.class);
    }

    public String extraerRol(String token) {
        return parsearToken(token).getPayload().get("rol", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Jws<Claims> parsearToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
