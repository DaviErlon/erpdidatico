package com.example.erpserver.services;

import com.example.erpserver.DTOs.FornecedorDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Fornecedor;
import com.example.erpserver.entities.Funcionario;
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

    private final FornecedoresRepositorio fornecedores;
    private final JwtUtil jwtUtil;

    private final ServicoLogAuditoria servicoLogAuditoria;

    public ServicoFornecedores(
            FornecedoresRepositorio fornecedores,
            JwtUtil jwtUtil,
            ServicoLogAuditoria servicoLogAuditoria
    ) {
        this.servicoLogAuditoria = servicoLogAuditoria;
        this.jwtUtil = jwtUtil;
        this.fornecedores = fornecedores;
    }

    // ---------- Adicionar Fornecedor ----------
    @Transactional
    public Optional<Fornecedor> adicionarFornecedor(String token, FornecedorDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        if (fornecedores.existsByCeoIdAndCpf(ceoId, dto.getCpf()) ||
                fornecedores.existsByCeoIdAndCnpj(ceoId, dto.getCnpj())) {
            return Optional.empty();
        }

        Ceo ceoReferencia = new Ceo();
        ceoReferencia.setId(ceoId);

        Funcionario emissor = new Funcionario();
        emissor.setId(emissorId);

        Fornecedor novoFornecedor = new Fornecedor();
        novoFornecedor.setCeo(ceoReferencia);
        novoFornecedor.setNome(dto.getNome());
        novoFornecedor.setCpf(dto.getCpf());
        novoFornecedor.setCnpj(dto.getCnpj());
        novoFornecedor.setTelefone(dto.getTelefone());

        novoFornecedor = fornecedores.save(novoFornecedor);

        servicoLogAuditoria.registrar(ceoReferencia, emissor, "ADIÇÃO", "FORNECEDOR", novoFornecedor.getId(), "NOVO FORNECEDOR CADASTRADO");

        return Optional.of(novoFornecedor);
    }

    // ---------- Atualizar Fornecedor ----------
    @Transactional
    public Optional<Fornecedor> atualizarFornecedor(String token, UUID id, FornecedorDTO dto) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        if (fornecedores.findByCeoIdAndCpf(ceoId, dto.getCpf())
                .filter(f -> !f.getId().equals(id))
                .isPresent()) {
            return Optional.empty(); // CPF já usado
        }

        if (fornecedores.findByCeoIdAndCnpj(ceoId, dto.getCnpj())
                .filter(f -> !f.getId().equals(id))
                .isPresent()) {
            return Optional.empty(); // CNPJ já usado
        }

        return fornecedores.findByCeoIdAndId(ceoId, id)
                .map(f -> {
                    f.setNome(dto.getNome());
                    f.setCpf(dto.getCpf());
                    f.setCnpj(dto.getCnpj());
                    f.setTelefone(dto.getTelefone());

                    Ceo ceoReferencia = new Ceo();
                    ceoReferencia.setId(ceoId);
                            
                    Funcionario emissor = new Funcionario();
                    emissor.setId(emissorId);

                    servicoLogAuditoria.registrar(ceoReferencia, emissor, "EDIÇÃO", "FORNECEDOR", f.getId(), "EDIÇÃO NOS DADOS DO FORNECEDOR");

                    return fornecedores.save(f);
                });
    }

    // ---------- Buscar Fornecedores (Paginação) ----------
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
        Specification<Fornecedor> spec = FornecedorSpecifications.comFiltros(ceoId, cpf, cnpj, nome, telefone);

        return PaginaDTO.from(fornecedores.findAll(spec, pageable));
    }

    // ---------- Remover Fornecedor ----------
    @Transactional
    public Optional<Fornecedor> removerFornecedor(
            String token,
            UUID fornecedorId
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        return fornecedores.findByCeoIdAndId(ceoId, fornecedorId)
                .map(f -> {
                    fornecedores.delete(f);
                    
                    Ceo ceoReferencia = new Ceo();
                    ceoReferencia.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(emissorId);
                    
                    servicoLogAuditoria.registrar(ceoReferencia, emissor, "REMOÇÃO", "FORNECEDOR", f.getId(), "REMOÇÃO DO FORNEDOR " + f.getNome());

                    return f;
                });
    }
}
