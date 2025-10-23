package com.example.erpserver.services;

import com.example.erpserver.DTOs.CadastroDTO;
import com.example.erpserver.entities.Ceo;

import com.example.erpserver.repositories.CeoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Service
public class ServicoCadastro {

    private final CeoRepositorio ceos;
    private final PasswordEncoder passwordEncoder;

    public ServicoCadastro(
            CeoRepositorio ceos,
            PasswordEncoder passwordEncoder
    ){
        this.ceos = ceos;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- Adicionar Ceo ----------
    @Transactional
    public Optional<Ceo> adicionarCeo(CadastroDTO dto) {

        if (ceos.existsByEmailOrCpf(dto.getEmail(), dto.getCpf())) {
            return Optional.empty();
        }

        Ceo novo = Ceo.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .plano(dto.getPlano())
                .senhaHash(passwordEncoder.encode(dto.getSenha()))
                .build();

        return Optional.of(ceos.save(novo));
    }


    // As funções de Remover e atualizar vao ficar pendentes pois exige-se uma futura role dev

}
