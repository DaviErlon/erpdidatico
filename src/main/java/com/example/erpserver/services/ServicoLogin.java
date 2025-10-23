package com.example.erpserver.services;

import com.example.erpserver.DTOs.RespostaDTO;
import com.example.erpserver.entities.TipoPlano;
import com.example.erpserver.repositories.CeoRepositorio;
import com.example.erpserver.repositories.FuncionariosRepositorio;
import com.example.erpserver.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ServicoLogin {

    private final CeoRepositorio ceos;
    private final FuncionariosRepositorio funcionarios;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public ServicoLogin(CeoRepositorio ceos, FuncionariosRepositorio funcionarios, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.ceos = ceos;
        this.funcionarios = funcionarios;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<RespostaDTO> autenticar(String email, String senha) {
        return autenticarCeo(email, senha)
                .or(() -> autenticarFuncionario(email, senha));
    }

    // --- Métodos auxiliares ---
    private Optional<RespostaDTO> autenticarCeo(String email, String senha) {

        return ceos.findByEmail(email)
                .filter(c -> passwordEncoder.matches(senha, c.getSenhaHash()))
                .map(c -> criarResposta(
                        c.getEmail(),
                        Set.of("CEO"),
                        c.getId(),
                        c.getId(),
                        true,
                        c.getPlano(),
                        c.getNome()
                ));
    }

    private Optional<RespostaDTO> autenticarFuncionario(String email, String senha) {

        return funcionarios.findByEmail(email)
                .filter(f -> passwordEncoder.matches(senha, f.getSenhaHash()))
                .map(f -> criarResposta(
                        f.getEmail(),
                        Set.of(f.getTipo().name()),
                        f.getId(),
                        f.getCeo().getId(),
                        false,
                        f.getCeo().getPlano(),
                        f.getNome()
                ));
    }

    private RespostaDTO criarResposta(String email, Set<String> roles, UUID userId, UUID ceoId, boolean assinante, TipoPlano plano, String nome) {
        return RespostaDTO.builder()
                .token(jwtUtil.gerarToken(email, roles, userId, ceoId))
                .assinante(assinante)
                .plano(plano)
                .nome(nome)
                .build();
    }

}
