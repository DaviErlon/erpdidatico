package com.example.erpserver.services;

import com.example.erpserver.DTOs.CadastroDTO;
import com.example.erpserver.entities.Assinante;

import com.example.erpserver.repository.AssinantesRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Service
public class ServicoCadastro {

    private final AssinantesRepositorio assinantes;
    private final PasswordEncoder passwordEncoder;

    public ServicoCadastro(AssinantesRepositorio assinantes, PasswordEncoder passwordEncoder){
        this.assinantes = assinantes;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- Adicionar Assinante ----------
    @Transactional
    public Optional<Assinante> addAssinante(CadastroDTO dto) {

        if (assinantes.findByEmail(dto.getEmail()).isPresent() || assinantes.findByCpf(dto.getCpf()).isPresent()) {
            return Optional.empty();
        }

        Assinante novo = new Assinante();
        novo.setNome(dto.getNome());
        novo.setEmail(dto.getEmail());
        novo.setCpf(dto.getCpf());
        novo.setPlano(dto.getPlano());

        novo.setSenhaHash(passwordEncoder.encode(dto.getSenha()));

        return Optional.of(assinantes.save(novo));
    }

    // As funções de Remover e atualizar vao ficar pendentes pois exige-se uma futura role dev

}
