package com.example.erpserver.specifications;

import com.example.erpserver.entities.Titulo;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TituloSpecifications {

    public static Specification<Titulo> doAssinante(Long assinanteId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assinante").get("id"), assinanteId);
    }

    public static Specification<Titulo> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null || cpf.isEmpty() ? null : criteriaBuilder.like(
                        root.get("pessoa").get("cpf"),
                        cpf + "%"
                );
    }

    public static Specification<Titulo> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null || nome.isEmpty() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("pessoa").get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Titulo> noPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, criteriaBuilder) -> {
            if (inicio == null || fim == null) return null;
            return criteriaBuilder.between(root.get("criadoEm"), inicio, fim);
        };
    }

    public static Specification<Titulo> pago(Boolean pago) {
        return (root, query, criteriaBuilder) -> {
            if (pago == null) return null;
            return pago ?
                    criteriaBuilder.isTrue(root.get("pago")) :
                    criteriaBuilder.isFalse(root.get("pago"));
        };
    }

    public static Specification<Titulo> aReceber() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.isFalse(root.get("pago")),
                        criteriaBuilder.greaterThan(root.get("valor"), 0)
                );
    }

    public static Specification<Titulo> aPagar() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.isFalse(root.get("pago")),
                        criteriaBuilder.lessThan(root.get("valor"), 0)
                );
    }

    public static Specification<Titulo> comFiltros(
            Long assinanteId,
            String cpf,
            String nome,
            LocalDateTime inicio,
            LocalDateTime fim,
            Boolean pago,
            Boolean aReceber,
            Boolean aPagar) {

        return Specification.allOf(
                doAssinante(assinanteId),
                comCpf(cpf),
                comNome(nome),
                noPeriodo(inicio, fim),
                pago(pago),
                aReceber != null && aReceber ? aReceber() : null,
                aPagar != null && aPagar ? aPagar() : null
        );
    }
}
