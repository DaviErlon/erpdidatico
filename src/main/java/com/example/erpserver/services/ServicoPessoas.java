package com.example.erpserver.services;

import com.example.erpserver.entities.Clientes;
import com.example.erpserver.DTOs.PessoaDTO;
import com.example.erpserver.repository.AssinantesRepositorio;
import com.example.erpserver.repository.PessoasRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.ClienteSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServicoPessoas {

    private final AssinantesRepositorio assinantesRepositorio;
    private final PessoasRepositorio repositorio;
    private final JwtUtil jwtUtil;

    public ServicoPessoas(
            PessoasRepositorio repositorio,
            JwtUtil jwtUtil,
            AssinantesRepositorio assinantesRepositorio
    ) {
        this.repositorio = repositorio;
        this.jwtUtil = jwtUtil;
        this.assinantesRepositorio = assinantesRepositorio;
    }

    // ---------- Adicionar Clientes ----------
    @Transactional
    public Optional<Clientes> addPessoa(PessoaDTO dto, String token) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return assinantesRepositorio.findById(assinanteId)
                .map(assinante -> {
                    Clientes clientes = new Clientes();
                    clientes.setCpf(dto.getCpf());
                    clientes.setFornecedor(dto.isFornecedor());
                    clientes.setNome(dto.getNome());
                    clientes.setCeo(assinante);
                    clientes.setContato(dto.getContato());
                    return repositorio.save(clientes);
                });
    }

    // ---------- Atualizar Clientes ----------
    @Transactional
    public Optional<Clientes> atualizarPessoa(String token, Long id, PessoaDTO dto) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, id)
                .map(pessoa -> {
                    pessoa.setNome(dto.getNome());
                    pessoa.setFornecedor(dto.isFornecedor());
                    pessoa.setContato(dto.getContato());
                    return repositorio.save(pessoa);
                });
    }

    // ---------- Buscar Pessoas (Paginação) ----------
    public Page<Clientes> buscarPessoas(
            String token,
            String cpf,
            String nome,
            Boolean fornecedor,
            int pagina,
            int tamanho
    ) {
        Long assinanteId = jwtUtil.extrairAdminId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Clientes> spec = ClienteSpecifications.comFiltros(assinanteId, cpf, nome, fornecedor);

        return repositorio.findAll(spec, pageable);
    }

    // ---------- Remover Clientes ----------
    @Transactional
    public Optional<Clientes> removerPorId(String token, Long id) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, id)
                .map(pessoa -> {
                    repositorio.deleteByAssinanteIdAndId(assinanteId, id);
                    return pessoa;
                });
    }
}

