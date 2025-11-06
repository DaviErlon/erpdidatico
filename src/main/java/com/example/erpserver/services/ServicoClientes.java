package com.example.erpserver.services;

import com.example.erpserver.DTOs.ClienteDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Cliente;
import com.example.erpserver.entities.Funcionario;
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

    private final ClientesRepositorio clientes;
    private final JwtUtil jwtUtil;

    private final ServicoLogAuditoria servicoLogAuditoria;

    public ServicoClientes(
            ClientesRepositorio clientes,
            JwtUtil jwtUtil,
            ServicoLogAuditoria servicoLogAuditoria
    ) {
        this.servicoLogAuditoria = servicoLogAuditoria;
        this.jwtUtil = jwtUtil;
        this.clientes = clientes;
    }

    // ---------- Adicionar Cliente ----------
    @Transactional
    public Optional<Cliente> adicionarCliente(String token, ClienteDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        if (clientes.existsByCeoIdAndCpf(ceoId, dto.getCpf())) {
            return Optional.empty();
        }

        Ceo ceoReferencia = new Ceo();
        ceoReferencia.setId(ceoId);

        Funcionario emissor = new Funcionario();
        emissor.setId(emissorId);

        Cliente novoCliente = new Cliente();
        novoCliente.setCeo(ceoReferencia);
        novoCliente.setNome(dto.getNome());
        novoCliente.setCpf(dto.getCpf());
        novoCliente.setTelefone(dto.getTelefone());

        novoCliente = clientes.save(novoCliente);

        servicoLogAuditoria.registrar(ceoReferencia, emissor, "ADIÇÃO", "CLIENTE", novoCliente.getId(), "NOVO CLIENTE CADASTRADO");

        return Optional.of(novoCliente);
    }

    // ---------- Atualizar Cliente ----------
    @Transactional
    public Optional<Cliente> atualizarCliente(String token, UUID id, ClienteDTO dto) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

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
                    
                    Ceo ceoReferencia = new Ceo();
                    ceoReferencia.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(emissorId);

                    servicoLogAuditoria.registrar(ceoReferencia, emissor, "EDIÇÃO", "CLIENTE", id, "DADOS DE CLIENTE EDITADOS");

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
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);
        
        return clientes.findByCeoIdAndId(ceoId, clienteId)
                .map(cl -> {

                    Ceo ceoReferencia = new Ceo();
                    ceoReferencia.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(emissorId);

                    servicoLogAuditoria.registrar(ceoReferencia, emissor, "EDIÇÃO", "CLIENTE", clienteId, "REMOÇÃO DO CLIENTE " + cl.getNome());

                    clientes.delete(cl);
                    return cl;
                });
    }
}
