package com.example.erpserver.specifications;

import com.example.erpserver.entities.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public class LogSpecifications {

    public static Specification<LogAuditoria> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ceo").get("id"), ceoId);
    }

    public static Specification<LogAuditoria> doEmissor(UUID funcionarioId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("emissor").get("id"), funcionarioId);
    }

    public static Specification<LogAuditoria> noPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, criteriaBuilder) -> {
            if (inicio == null || fim == null) return null;
            return criteriaBuilder.between(root.get("criadoEm"), inicio, fim);
        };
    }

    public static Specification<LogAuditoria> comFiltros(
            UUID ceoId,
            UUID emissorId,
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        Specification<LogAuditoria> spec = doCeo(ceoId);

        return Specification.allOf(
                doCeo(ceoId),
                noPeriodo(inicio, fim),
                doEmissor(emissorId)
        );
    }
}
