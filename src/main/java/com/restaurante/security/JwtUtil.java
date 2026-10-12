package com.restaurante.security;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generarToken(String email, List<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String extraerEmail(String token) {
        return parsear(token).getPayload().getSubject();
    }

    public boolean esValido(String token) {
        try {
            parsear(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Jws<Claims> parsear(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
    }
}
