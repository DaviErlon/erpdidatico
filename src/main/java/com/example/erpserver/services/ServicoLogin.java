package com.example.erpserver.services;

import com.example.erpserver.DTOs.RespostaDTO;
import com.example.erpserver.entities.TipoEspecializacao;
import com.example.erpserver.entities.TipoPlano;
import com.example.erpserver.repositories.FuncionariosRepositorio;
import com.example.erpserver.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ServicoLogin {

    private final FuncionariosRepositorio funcionarios;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final ServicoLogAuditoria servicoLogAuditoria;

    public ServicoLogin(
            FuncionariosRepositorio funcionarios,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            ServicoLogAuditoria servicoLogAuditoria
    ) {
        this.servicoLogAuditoria = servicoLogAuditoria;
        this.funcionarios = funcionarios;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<RespostaDTO> autenticar(String email, String senha) {
        return funcionarios.findByEmail(email)
                .filter(f -> passwordEncoder.matches(senha, f.getSenhaHash()))
                .map(f -> {

                        servicoLogAuditoria.registrar(f.getCeo(), f, "LOGIN", "Funcionario", f.getId(), "LOGIN DE USUARIO NO ERP");

                        return criarResposta(
                            f.getEmail(),
                            Set.of(f.getTipo().name()),
                            f.getId(),
                            f.getCeo().getId(),
                            f.getCeo().getPlano(),
                            f.getNome()
                        );
                    }   
                );
    }

    private RespostaDTO criarResposta(
            String email,
            Set<String> roles,
            UUID userId,
            UUID ceoId,
            TipoPlano plano,
            String nome
    ) {
        RespostaDTO resposta = new RespostaDTO();
        resposta.setToken(jwtUtil.gerarToken(email, roles, userId, ceoId));
        resposta.setTipo(TipoEspecializacao.valueOf(roles.iterator().next()));
        resposta.setPlano(plano);
        resposta.setNome(nome);
        return resposta;
    }
}
