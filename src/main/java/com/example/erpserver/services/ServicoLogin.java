package com.example.erpserver.services;

import com.example.erpserver.DTOs.RespostaDTO;
import com.example.erpserver.repository.CeoRepositorio;
import com.example.erpserver.repository.FuncionariosRepositorio;
import com.example.erpserver.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

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
                .map(c -> {

                    RespostaDTO resposta = RespostaDTO
                            .builder()
                            .token(
                                    jwtUtil.gerarToken(
                                            c.getEmail(),
                                            Set.of("CEO"),
                                            c.getId(),
                                            c.getId()
                                    )
                            )
                            .assinante(true)
                            .plano(c.getPlano())
                            .nome(c.getNome())
                            .token(c.getTokenAutorizacao())
                            .build();

                    return resposta;
                });
    }

    private Optional<RespostaDTO> autenticarFuncionario(String email, String senha) {
        return funcionarios.findByEmail(email)
                .filter(f -> passwordEncoder.matches(senha, f.getSenhaHash()))
                .map(f -> {
                    RespostaDTO resposta = RespostaDTO
                            .builder()
                            .token(
                                    jwtUtil.gerarToken(
                                            f.getEmail(),
                                            Set.of(f.getTipo().name()),
                                            f.getId(),
                                            f.getCeo().getId()
                                    )
                            )
                            .assinante(false)
                            .plano(f.getCeo().getPlano())
                            .nome(f.getNome())
                            .build();

                    return resposta;
                });
    }
}
