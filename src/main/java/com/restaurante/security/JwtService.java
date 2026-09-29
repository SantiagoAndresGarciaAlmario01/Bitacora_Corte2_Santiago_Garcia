package com.restaurante.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida los tokens JWT (firmados con HMAC-SHA256).
 *
 * <p>El token lleva el correo del usuario como "subject" y su rol como claim
 * adicional. No guarda nada en el servidor: cada peticion trae su token.</p>
 */
@Service
public class JwtService {

    static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${seguridad.jwt.secreto}") String secretoBase64,
                      @Value("${seguridad.jwt.expiracion-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String correo, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(correo)
                .claim(CLAIM_ROL, rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el correo del token; lanza JwtException si el token es invalido o vencio. */
    public String extraerCorreo(String token) {
        return leerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return leerClaims(token).get(CLAIM_ROL, String.class);
    }

    public boolean esValidoPara(String token, String correoEsperado) {
        try {
            Claims claims = leerClaims(token);
            return correoEsperado.equalsIgnoreCase(claims.getSubject())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
