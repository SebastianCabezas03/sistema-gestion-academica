
package com.reinodeloscielos.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Locale;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "La clave secreta JWT no puede estar vacía"
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalArgumentException(
                    "La duración del token JWT debe ser mayor que cero"
            );
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);

            this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "La clave secreta JWT no es válida. Debe estar codificada en Base64 y tener al menos 256 bits",
                    e
            );
        }

        this.expirationMs = expirationMs;
    }

    /**
     * Genera un token JWT para un usuario autenticado.
     */
    public String generarToken(String username, String rol) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio para generar el token"
            );
        }

        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio para generar el token"
            );
        }

        Date ahora = new Date();

        Date expiracion = new Date(
                ahora.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(username.trim())
                .claim("rol", rol.trim().toUpperCase(Locale.ROOT))
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Valida la firma y la fecha de expiración del token.
     */
    public Claims validarToken(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token JWT no puede estar vacío"
            );
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
