package com.example.erpserver.services;

import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.LogAuditoria;
import com.example.erpserver.repositories.FuncionariosRepositorio;
import com.example.erpserver.repositories.LogAuditoriaRepository;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.LogSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ServicoLogAuditoria {

    private final LogAuditoriaRepository logs;
    private final JwtUtil jwtUtil;
    private final FuncionariosRepositorio funcionarios;

    public ServicoLogAuditoria(
            LogAuditoriaRepository logs,
            JwtUtil jwtUtil,
            FuncionariosRepositorio funcionarios
    ) {
        this.funcionarios = funcionarios;
        this.logs = logs;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void registrar(Ceo ceo, Funcionario emissor, String acao, String entidade, UUID entidadeId, String detalhes) {

        emissor = funcionarios.findByCeoIdAndId(ceo.getId(), emissor.getId()).orElse(null);
        if (emissor == null) return;

        LogAuditoria log = new LogAuditoria();
        log.setCeo(ceo);
        log.setFuncionario(emissor);
        log.setNome(emissor.getNome());
        log.setEmail(emissor.getEmail());
        log.setTelefone(emissor.getTelefone());
        log.setCpf(emissor.getCpf());
        log.setSetor(emissor.getSetor());
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setDetalhes(detalhes);

        logs.save(log);
    }

    public PaginaDTO<LogAuditoria> buscarLogs(
            String token,
            String nome,
            String cpf,
            String telefone,
            String acao,
            LocalDateTime inicio,
            LocalDateTime fim,
            int pagina,
            int tamanho
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<LogAuditoria> spec = LogSpecifications.comFiltros(ceoId, nome, cpf, telefone, acao, inicio, fim);

        return PaginaDTO.from(logs.findAll(spec, pageable));
    }
}
