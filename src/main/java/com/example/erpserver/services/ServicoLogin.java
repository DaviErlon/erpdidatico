package com.example.erpserver.services;

import com.example.erpserver.models.Usuario;
import com.example.erpserver.repository.Repositorio;
import com.example.erpserver.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ServicoLogin {

    private static final Logger logger = LoggerFactory.getLogger(ServicoLogin.class);

    private static final String USERNAME_ADMIN = "admin";
    private static final String SENHA_ADMIN = "admin123";

    private final JwtUtil jwtUtil;
    private final Repositorio repositorio;
    private final List<Usuario> usuarios;
    private final PasswordEncoder passwordEncoder;

    public ServicoLogin(JwtUtil jwtUtil, Repositorio repositorio, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.repositorio = repositorio;
        this.usuarios = repositorio.carregarUsuarios();
    }

    // ---------- Persistência ----------
    public void salvarJson() {
        repositorio.salvarUsuarios(usuarios);
    }

    public Optional<String> autenticar(String username, String senha) {
        // --- ADMIN FIXO ---
        if (USERNAME_ADMIN.equals(username) && SENHA_ADMIN.equals(senha)) {
            String token = jwtUtil.generateToken("admin", Set.of("ADMIN"));
            return Optional.of(token);
        }

        // --- USUÁRIO COMUM ---
        Optional<Usuario> userOpt = usuarios.stream()
                .filter(u -> u.getUsername().equals(username) && passwordEncoder.matches(senha, u.getSenha()))
                .findFirst();

        if (userOpt.isPresent()) {
            String token = jwtUtil.generateToken(username, Set.of("USER"));
            return Optional.of(token);
        }

        // --- CREDENCIAIS INVÁLIDAS ---
        return Optional.empty();
    }
}
