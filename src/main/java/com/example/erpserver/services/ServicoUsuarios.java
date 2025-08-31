package com.example.erpserver.services;

import com.example.erpserver.models.Usuario;
import com.example.erpserver.repository.Repositorio;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Getter
public class ServicoUsuarios {

    private static final Logger logger = LoggerFactory.getLogger(ServicoUsuarios.class);

    private final List<Usuario> usuarios;
    private final Repositorio repositorio;
    private final PasswordEncoder passwordEncoder;

    public ServicoUsuarios(Repositorio repositorio, PasswordEncoder passwordEncoder) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
        this.usuarios = new CopyOnWriteArrayList<>(repositorio.carregarUsuarios());
    }

    // ---------- Persistência ----------
    public void salvarJson() {
        repositorio.salvarUsuarios(usuarios);
    }

    // ---------- Usuários ----------
    public Usuario addUsuario(String username, String senha, String role) {
        // evita duplicidade
        if (usuarios.stream().anyMatch(u -> u.getUsername().equals(username))) {
            logger.warn("Usuário já existe: {}", username);
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(role != null ? role : "USER");

        usuarios.add(usuario);
        salvarJson();
        logger.info("Usuário adicionado com sucesso: {}", username);
        return usuario;
    }

    public Optional<Usuario> getUsuarioByUsername(String username) {
        return usuarios.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public List<Usuario> getUsuariosPorRole(String role) {
        return usuarios.stream()
                .filter(u -> u.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    public Optional<Usuario> removeUsuario(String username) {
        Optional<Usuario> u = getUsuarioByUsername(username);
        u.ifPresent(usuarios::remove);
        if (u.isEmpty()) {
            logger.warn("Não existe usuário com esse username: {}", username);
        } else {
            salvarJson();
            logger.info("Usuário removido com sucesso: {}", username);
        }
        return u;
    }
}
