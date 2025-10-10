package com.example.erpserver.services;

import com.example.erpserver.DTOs.CadastroMembroDTO;
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

    // ---------- Adicionar Gerente ----------
    @Transactional
    public Optional<Gerente> addMembro(String token, CadastroMembroDTO dto) {
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

                    Gerente gerente = new Gerente();
                    gerente.setNome(dto.getNome());
                    gerente.setEmail(dto.getEmail());
                    gerente.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
                    gerente.setCeo(assinante);
                    return Optional.of(membros.save(gerente));
                });
    }


    // ---------- Editar Gerente ----------
    @Transactional
    public Optional<Gerente> editarMembro(String token, Long membroId, CadastroMembroDTO dto) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return membros.findById(membroId)
                .filter(m -> m.getCeo().getId().equals(assinanteId))
                .map(gerente -> {
                    gerente.setNome(dto.getNome());
                    gerente.setEmail(dto.getEmail());
                    gerente.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
                    return membros.save(gerente);
                });
    }

    // ---------- Remover Gerente ----------
    @Transactional
    public Optional<Gerente> removerMembro(String token, Long membroId) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return membros.findById(membroId)
                .filter(m -> m.getCeo().getId().equals(assinanteId))
                .map(gerente -> {
                    membros.delete(gerente);
                    return gerente;
                });
    }

    // ---------- Buscar Membros por Nome (Paginação) ----------
    public Page<Gerente> buscarPorNome(String token, String prefixoNome, int pagina, int tamanho) {
        Long assinanteId = jwtUtil.extrairAdminId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        return membros.findByAssinanteIdAndNomeStartingWithIgnoreCase(assinanteId, prefixoNome, pageable);
    }

}
