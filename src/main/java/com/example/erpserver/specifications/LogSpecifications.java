package com.example.erpserver.specifications;

import com.example.erpserver.entities.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class LogSpecifications {

    public static Specification<LogAuditoria> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ceo").get("id"), ceoId);
    }

    public static Specification<LogAuditoria> comCpf(String cpf) {
        return (root, query, criteriaBuilder) -> (cpf == null || cpf.isEmpty()) ? null
                : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<LogAuditoria> comTelefone(String telefone) {
        return (root, query, criteriaBuilder) -> (telefone == null || telefone.isEmpty()) ? null
                : criteriaBuilder.like(root.get("telefone"), telefone + "%");
    }

    public static Specification<LogAuditoria> comNome(String nome) {
        return (root, query, criteriaBuilder) -> (nome == null || nome.isEmpty()) ? null
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%");
    }

    public static Specification<LogAuditoria> deAcao(String acao) {
        return (root, query, criteriaBuilder) -> (acao == null || acao.isEmpty()) ? null
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("acao")),
                        "%" + acao.toLowerCase() + "%");
    }

    public static Specification<LogAuditoria> noPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, criteriaBuilder) -> {
            if (inicio == null || fim == null)
                return null;

            // converte LocalDateTime → OffsetDateTime
            var zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(inicio);

            OffsetDateTime inicioOdt = inicio.atOffset(zoneOffset);
            OffsetDateTime fimOdt = fim.atOffset(zoneOffset);

            return criteriaBuilder.between(root.get("criadoEm"), inicioOdt, fimOdt);
        };
    }

    public static Specification<LogAuditoria> comFiltros(
            UUID ceoId,
            String nome,
            String cpf,
            String telefone,
            String acao,
            LocalDateTime inicio,
            LocalDateTime fim) {

        return Specification.allOf(
                doCeo(ceoId),
                comNome(nome),
                comCpf(cpf),
                comTelefone(telefone),
                noPeriodo(inicio, fim),
                deAcao(acao));
    }
}
