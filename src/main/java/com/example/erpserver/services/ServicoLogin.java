package com.example.erpserver.services;

import com.example.erpserver.repository.AssinantesRepositorio;
import com.example.erpserver.repository.MembrosRepositorio;
import com.example.erpserver.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class ServicoLogin {

    private final AssinantesRepositorio assinantes;
    private final MembrosRepositorio membros;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public ServicoLogin(AssinantesRepositorio assinantes, MembrosRepositorio membros, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.assinantes = assinantes;
        this.membros = membros;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<String> autenticar(String email, String senha) {
        return autenticarAssinante(email, senha)
                .or(() -> autenticarMembro(email, senha));
    }

    // --- Métodos auxiliares ---
    private Optional<String> autenticarAssinante(String email, String senha) {
        return assinantes.findByEmail(email)
                .filter(a -> passwordEncoder.matches(senha, a.getSenhaHash()))
                .map(a -> jwtUtil.gerarToken(
                        a.getEmail(),
                        Set.of("ADMIN"),
                        a.getId(),
                        a.getId()
                ));
    }

    private Optional<String> autenticarMembro(String email, String senha) {
        return membros.findByEmail(email)
                .filter(m -> passwordEncoder.matches(senha, m.getSenhaHash()))
                .map(m -> jwtUtil.gerarToken(
                        m.getEmail(),
                        Set.of("USER"),
                        m.getId(),
                        m.getAssinante().getId()
                ));
    }
}
