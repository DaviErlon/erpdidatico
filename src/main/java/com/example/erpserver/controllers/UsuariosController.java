package com.example.erpserver.controllers;

import com.example.erpserver.models.Usuario;
import com.example.erpserver.services.ServicoUsuarios;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuariosController {

    @Autowired
    private ServicoUsuarios service;

    // ------ ENDPOINTS ::::::::: USUÁRIOS --------

    @GetMapping
    public List<Usuario> getUsuarios() {
        return service.getUsuarios();
    }

    @GetMapping("/admins")
    public List<Usuario> getAdmins() {
        return service.getUsuariosPorRole("ADMIN");
    }

    @GetMapping("/users")
    public List<Usuario> getUsers() {
        return service.getUsuariosPorRole("USER");
    }

    @GetMapping("/{username}")
    public Optional<Usuario> getUsuario(@PathVariable String username) {
        return service.getUsuarioByUsername(username);
    }

    //------- POST --------
    @PostMapping
    public Usuario addUsuario(@RequestBody @Valid Usuario usuario) {
        // Retorna null se não for possível criar (usuário duplicado)
        return service.addUsuario(usuario.getUsername(), usuario.getSenha(), usuario.getRole());
    }

    //------- DELETE --------
    @DeleteMapping("/{username}")
    public Optional<Usuario> deleteUsuario(@PathVariable String username) {
        return service.removeUsuario(username);
    }
}
