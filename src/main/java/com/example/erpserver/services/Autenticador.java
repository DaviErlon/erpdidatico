package com.example.erpserver.services;

import com.example.erpserver.models.Usuario;
import com.example.erpserver.repository.Repositorio;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class Autenticador {

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private Repositorio repositorio;
    private List<Usuario> usuarios;

    public Autenticador(Repositorio repositorio){
        this.repositorio = repositorio;
        this.usuarios = new CopyOnWriteArrayList<> (repositorio.carregarUsuarios());
    }
    public Optional<String> logarUsuario(Usuario usuario) {
        return usuarios.stream()
                .filter(u -> u.getLogin().equals(usuario.getLogin())
                        && u.getSenha().equals(usuario.getSenha()))
                .findFirst()
                .map(u -> gerarToken(u.getLogin()));
    }

    public boolean verificarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            String login = claims.getSubject();
            Date expiration = claims.getExpiration();

            return login != null && expiration.after(new Date());
        } catch (ExpiredJwtException e) {
            System.out.println("⚠️ Token expirado!");
            return false;
        } catch (SignatureException e) {
            System.out.println("⚠️ Assinatura inválida!");
            return false;
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao validar token: " + e.getMessage());
            return false;
        }
    }

    private String gerarToken(String login) {
        return Jwts.builder()
                .setSubject(login)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}

