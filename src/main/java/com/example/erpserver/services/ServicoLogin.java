package com.example.erpserver.services;

import com.example.erpserver.DTOs.RespostaDTO;
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

    public Optional<RespostaDTO> autenticar(String email, String senha) {
        return autenticarAssinante(email, senha)
                .or(() -> autenticarMembro(email, senha));
    }

    // --- Métodos auxiliares ---
    private Optional<RespostaDTO> autenticarAssinante(String email, String senha) {

        return assinantes.findByEmail(email)
                .filter(a -> passwordEncoder.matches(senha, a.getSenhaHash()))
                .map(a -> {

                    RespostaDTO resposta = new RespostaDTO();

                    resposta.setToken(jwtUtil.gerarToken(
                            a.getEmail(),
                            Set.of("ADMIN"),
                            a.getId(),
                            a.getId()
                        )
                    );
                    resposta.setAssinante(true);
                    resposta.setPlano(a.getPlano());
                    resposta.setNome(a.getNome());

                    return resposta;
                });
    }

    private Optional<RespostaDTO> autenticarMembro(String email, String senha) {
        return membros.findByEmail(email)
                .filter(m -> passwordEncoder.matches(senha, m.getSenhaHash()))
                .map(m -> {
                    RespostaDTO resposta = new RespostaDTO();

                    resposta.setToken(
                            jwtUtil.gerarToken(
                                    m.getEmail(),
                                    Set.of("USER"),
                                    m.getId(),
                                    m.getCeo().getId()
                            )
                    );

                    resposta.setAssinante(false);
                    resposta.setPlano(m.getCeo().getPlano());
                    resposta.setNome(m.getNome());

                    return resposta;
                });
    }
}
