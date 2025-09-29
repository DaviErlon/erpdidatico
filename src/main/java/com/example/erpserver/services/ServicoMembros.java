package com.example.erpserver.services;

import com.example.erpserver.DTOs.CadastroMembroDTO;
import com.example.erpserver.entities.Membro;
import com.example.erpserver.repository.AssinantesRepositorio;
import com.example.erpserver.repository.MembrosRepositorio;
import com.example.erpserver.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServicoMembros {

    private final MembrosRepositorio membros;
    private final AssinantesRepositorio assinantes;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public ServicoMembros(AssinantesRepositorio assinantes, MembrosRepositorio membros, JwtUtil jwtUtil) {
        this.assinantes = assinantes;
        this.membros = membros;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Criptografia da senha
    }

    // ---------- Adicionar Membro ----------
    @Transactional
    public Optional<Membro> addMembro(String token, CadastroMembroDTO dto) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        // Se já existe membro com o mesmo email, retorna vazio
        if (membros.findByEmail(dto.getEmail()).isPresent()) {
            return Optional.empty();
        }

        return assinantes.findById(assinanteId)
                .flatMap(assinante -> {

                    // implementação de planos
                    int limite = switch (assinante.getPlano()) {
                        case 2 -> 20;
                        case 1 -> 10;
                        default -> 5;
                    };

                    long membrosAtuais = membros.countByAssinanteId(assinanteId);
                    if (membrosAtuais >= limite) {
                        return Optional.empty();
                    }

                    Membro membro = new Membro();
                    membro.setNome(dto.getNome());
                    membro.setEmail(dto.getEmail());
                    membro.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
                    membro.setAssinante(assinante);
                    return Optional.of(membros.save(membro));
                });
    }


    // ---------- Editar Membro ----------
    @Transactional
    public Optional<Membro> editarMembro(String token, Long membroId, CadastroMembroDTO dto) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return membros.findById(membroId)
                .filter(m -> m.getAssinante().getId().equals(assinanteId))
                .map(membro -> {
                    membro.setNome(dto.getNome());
                    membro.setEmail(dto.getEmail());
                    membro.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
                    return membros.save(membro);
                });
    }

    // ---------- Remover Membro ----------
    @Transactional
    public Optional<Membro> removerMembro(String token, Long membroId) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return membros.findById(membroId)
                .filter(m -> m.getAssinante().getId().equals(assinanteId))
                .map(membro -> {
                    membros.delete(membro);
                    return membro;
                });
    }

    // ---------- Buscar Membros por Nome (Paginação) ----------
    public Page<Membro> buscarPorNome(String token, String prefixoNome, int pagina, int tamanho) {
        Long assinanteId = jwtUtil.extrairAdminId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        return membros.findByAssinanteIdAndNomeStartingWithIgnoreCase(assinanteId, prefixoNome, pageable);
    }

}
