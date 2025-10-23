package com.example.erpserver.services;

import com.example.erpserver.DTOs.ClienteDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Cliente;
import com.example.erpserver.repositories.CeoRepositorio;
import com.example.erpserver.repositories.ClientesRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.ClienteSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ServicoClientes {

    private final CeoRepositorio ceos;
    private final ClientesRepositorio clientes;
    private final JwtUtil jwtUtil;

    public ServicoClientes(
            ClientesRepositorio clientes,
            JwtUtil jwtUtil,
            CeoRepositorio ceos
    ) {
        this.ceos = ceos;
        this.jwtUtil = jwtUtil;
        this.clientes = clientes;
    }

    // ---------- Adicionar Cliente ----------
    @Transactional
    public Optional<Cliente> adicionarCliente(String token, ClienteDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        if (clientes.existsByCeoIdAndCpf(ceoId, dto.getCpf())) {
            return Optional.empty();
        }

        Cliente novoCliente = Cliente.builder()
                .ceo(Ceo.builder().id(ceoId).build())
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .telefone(dto.getTelefone())
                .build();

        return Optional.of(clientes.save(novoCliente));
    }

    // ---------- Atualizar Cliente ----------
    @Transactional
    public Optional<Cliente> atualizarCliente(String token, UUID id, ClienteDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        if (clientes.findByCeoIdAndCpf(ceoId, dto.getCpf())
                .filter(c -> !c.getId().equals(id))
                .isPresent()) {
            return Optional.empty(); // CPF já usado por outro cliente
        }

        return clientes.findByCeoIdAndId(ceoId, id)
                .map(cl -> {
                    cl.setNome(dto.getNome());
                    cl.setCpf(dto.getCpf());
                    cl.setTelefone(dto.getTelefone());
                    return clientes.save(cl);
                });
    }

    // ---------- Buscar Cliente (Paginação) ----------
    public PaginaDTO<Cliente> buscarClientes(
            String token,
            String cpf,
            String nome,
            String telefone,
            int pagina,
            int tamanho
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Cliente> spec = ClienteSpecifications.comFiltros(ceoId, cpf, nome, telefone);

        return PaginaDTO.from(clientes.findAll(spec, pageable));
    }

    // ---------- Remover Cliente ----------
    @Transactional
    public Optional<Cliente> removerCliente(
            String token,
            UUID clienteId
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return clientes.findByCeoIdAndId(ceoId, clienteId)
                .map(cl -> {
                    clientes.delete(cl);
                    return cl;
                });
    }
}

