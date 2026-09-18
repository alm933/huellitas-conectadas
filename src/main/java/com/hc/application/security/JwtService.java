package com.hc.application.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;


@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration) {

        this.secretKey = Keys.hmacShaKeyFor(
                java.util.Base64.getDecoder().decode(secret)
        );

        this.expiration = expiration;
    }

    public String generarToken(UserDetails usuario) {

        Date ahora = new Date();
        Date fechaExpiracion = new Date(
                ahora.getTime() + expiration
        );

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", usuario.getAuthorities())
                .issuedAt(ahora)
                .expiration(fechaExpiracion)
                .signWith(secretKey)
                .compact();
    }

    public String extraerUsername(String token) {
    return obtenerClaims(token)
            .getPayload()
            .getSubject();
}

public boolean esValido(
        String token,
        UserDetails usuario) {

    try {
        Claims claims = obtenerClaims(token).getPayload();

        String username = claims.getSubject();
        Date expiracion = claims.getExpiration();

        return username.equals(usuario.getUsername())
                && expiracion.after(new Date());

    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}

private Jws<Claims> obtenerClaims(String token) {
    return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token);
}






}