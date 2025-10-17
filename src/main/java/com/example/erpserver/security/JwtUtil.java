package com.example.erpserver.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key SECRET_KEY;

    @PostConstruct
    public void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(secretString.getBytes());
    }

    // -------------------- Geração do token --------------------
    public String gerarToken(String username, Set<String> roles, UUID userId, UUID adminId) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim("roles", String.join(",", roles))
                .claim("userId", userId); // id do usuário

        // Se for membro, adiciona id do admin
        if (adminId != null) {
            builder.claim("adminId", adminId);
        }

        return builder
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // -------------------- Extração de dados --------------------
    public String extrairEmail(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public Set<String> extrairRoles(String token) {
        String rolesString = (String) parseToken(token).getBody().get("roles");
        return Set.of(rolesString.split(","));
    }

    public UUID extrairCeoId(String token) {
        Object adminId = parseToken(token).getBody().get("adminId");
        return adminId != null ? UUID.fromString(adminId.toString()) : null;
    }

    // -------------------- Validação --------------------
    public boolean tokenValido(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // -------------------- Parser --------------------
    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
    }
}
