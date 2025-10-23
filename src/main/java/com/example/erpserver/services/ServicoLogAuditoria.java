package com.example.erpserver.services;

import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.LogAuditoria;
import com.example.erpserver.repositories.CeoRepositorio;
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

import java.util.UUID;

@Service
public class ServicoLogAuditoria {

    private final LogAuditoriaRepository logs;
    private final CeoRepositorio ceos;
    private final FuncionariosRepositorio funcionarios;
    private final JwtUtil jwtUtil;

    public ServicoLogAuditoria(
            LogAuditoriaRepository logs,
            CeoRepositorio ceos,
            FuncionariosRepositorio funcionarios,
            JwtUtil jwtUtil
    ){
        this.logs = logs;
        this.funcionarios = funcionarios;
        this.ceos = ceos;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void registrar(Ceo ceo, Funcionario emissor, String acao, String entidade, UUID entidadeId, String detalhes) {
        logs.save(LogAuditoria.builder()
                .ceo(ceo)
                .funcionario(emissor)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .detalhes(detalhes)
                .build()
        );
    }


    public PaginaDTO<LogAuditoria> buscarLogs(
            String token,
            UUID emissorId,
            LocalDateTime inicio,
            LocalDateTime fim,
            int pagina,
            int tamanho
    ){
        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<LogAuditoria> spec = LogSpecifications.comFiltros(ceoId, emissorId, inicio, fim);

        return PaginaDTO.from(logs.findAll(spec, pageable));
    }
}
