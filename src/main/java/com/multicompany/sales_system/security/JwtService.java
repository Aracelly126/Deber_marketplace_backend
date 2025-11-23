package com.multicompany.sales_system.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;               
    @Value("${app.jwt.exp-minutes:60}")
    private int expMinutes;

    private SecretKey key() {
        byte[] bytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(Long userId, String cedula, String email, String role, String estado) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("cedula", cedula);
        claims.put("role", role);
        claims.put("estado", estado);
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + expMinutes * 60_000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            claims(token); // lanza excepción si es inválido/expirado
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        Object v = claims(token).get("uid");
        return (v instanceof Number) ? ((Number) v).longValue() : Long.valueOf(v.toString());
    }

    public String getRole(String token) {
        Object v = claims(token).get("role");
        return v == null ? null : v.toString();
    }

    public String getSubjectEmail(String token) {
        return claims(token).getSubject();
    }

    public String extractCedula(String token) {
        Object cedula = claims(token).get("cedula");
        return cedula != null ? cedula.toString() : null;
    }

    public String getEstado(String token) {
        Object estado = claims(token).get("estado");
        return estado != null ? estado.toString() : null;
    }
}
