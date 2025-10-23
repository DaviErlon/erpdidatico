package com.example.erpserver.services;

import com.example.erpserver.DTOs.FornecedorDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Fornecedor;
import com.example.erpserver.repositories.CeoRepositorio;
import com.example.erpserver.repositories.FornecedoresRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.FornecedorSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ServicoFornecedores {

    private final CeoRepositorio ceos;
    private final FornecedoresRepositorio fornecedores;
    private final JwtUtil jwtUtil;

    public ServicoFornecedores(
            FornecedoresRepositorio fornecedores,
            JwtUtil jwtUtil,
            CeoRepositorio ceos
    ) {
        this.ceos = ceos;
        this.jwtUtil = jwtUtil;
        this.fornecedores = fornecedores;
    }

    // ---------- Adicionar Fornecedor ----------
    @Transactional
    public Optional<Fornecedor> adicionarFornecedor(String token, FornecedorDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        if (fornecedores.existsByCeoIdAndCpf(ceoId, dto.getCpf()) || fornecedores.existsByCeoIdAndCnpj(ceoId, dto.getCnpj())) {
            return Optional.empty();
        }

        Fornecedor novoFornecedor = Fornecedor.builder()
                .ceo(Ceo.builder().id(ceoId).build())
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .telefone(dto.getTelefone())
                .build();

        return Optional.of(fornecedores.save(novoFornecedor));

    }

    // ---------- Atualizar Fornecedor ----------
    @Transactional
    public Optional<Fornecedor> atualizarCliente(String token, UUID id, FornecedorDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        if (fornecedores.findByCeoIdAndCpf(ceoId, dto.getCpf())
                .filter(f -> !f.getId().equals(id))
                .isPresent()) {
            return Optional.empty(); // CPF já usado
        }

        if (fornecedores.findByCeoIdAndCnpj(ceoId, dto.getCnpj())
                .filter(f -> !f.getId().equals(id))
                .isPresent()) {
            return Optional.empty(); // CPF já usado
        }

        return fornecedores.findByCeoIdAndId(ceoId, id)
                .map(f -> {
                    f.setNome(dto.getNome());
                    f.setCpf(dto.getCpf());
                    f.setCnpj(dto.getCnpj());
                    f.setTelefone(dto.getTelefone());
                    return fornecedores.save(f);
                });
    }

    // ---------- Buscar Forncedores (Paginação) ----------
    public PaginaDTO<Fornecedor> buscarFornecedores(
            String token,
            String cpf,
            String cnpj,
            String nome,
            String telefone,
            int pagina,
            int tamanho
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Fornecedor> spec = FornecedorSpecifications.comFiltros(ceoId, cpf, cnpj, telefone, nome);

        return PaginaDTO.from(fornecedores.findAll(spec, pageable));
    }

    // ---------- Remover Fornecodor ----------
    @Transactional
    public Optional<Fornecedor> removerFornecedor(
            String token,
            UUID fornecedorId
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return fornecedores.findByCeoIdAndId(ceoId, fornecedorId)
                .map(f -> {
                    fornecedores.delete(f);
                    return f;
                });
    }
}

