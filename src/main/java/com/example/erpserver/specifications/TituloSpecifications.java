package com.example.erpserver.specifications;

import com.example.erpserver.entities.Titulo;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class TituloSpecifications {

    public static Specification<Titulo> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ceo").get("id"), ceoId);
    }

    public static Specification<Titulo> doEmissor(UUID funcionarioId) {
        return (root, query, criteriaBuilder) ->
                funcionarioId == null ? null : criteriaBuilder.equal(
                        root.get("emissor").get("id"),
                        funcionarioId
                );
    }

    public static Specification<Titulo> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null || cpf.isEmpty() ? null : criteriaBuilder.like(
                        root.get("cpf"),
                        cpf + "%"
                );
    }

    public static Specification<Titulo> comCnpj(String cnpj) {
        return (root, query, criteriaBuilder) ->
                cnpj == null || cnpj.isEmpty() ? null : criteriaBuilder.like(
                        root.get("cnpj"),
                        cnpj + "%"
                );
    }

    public static Specification<Titulo> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null || nome.isEmpty() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
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

    public static Specification<Titulo> aprovado(Boolean aprovado) {
        return (root, query, criteriaBuilder) -> {
            if (aprovado == null) return null;
            return aprovado ?
                    criteriaBuilder.isTrue(root.get("aprovado")) :
                    criteriaBuilder.isFalse(root.get("aprovado"));
        };
    }

    public static Specification<Titulo> recebido(Boolean recebido) {
        return (root, query, criteriaBuilder) -> {
            if (recebido == null) return null;
            return recebido ?
                    criteriaBuilder.isTrue(root.get("recebidoNoEstoque")) :
                    criteriaBuilder.isFalse(root.get("recebidoNoEstoque"));
        };
    }

    public static Specification<Titulo> comTelefone(String telefone) {
        return (root, query, criteriaBuilder) ->
                (telefone == null || telefone.isEmpty()) ? null : criteriaBuilder.like(root.get("telefone"), telefone + "%");
    }

    public static Specification<Titulo> comFiltros(
            UUID ceoId,
            String cpf,
            String cnpj,
            String nome,
            String telefone,
            LocalDateTime inicio,
            LocalDateTime fim,
            Boolean aprovado,
            Boolean pago,
            Boolean recebido,
            UUID emissorId
    ) {

        return Specification.allOf(
                doCeo(ceoId),
                comCpf(cpf),
                comCnpj(cnpj),
                comNome(nome),
                comTelefone(telefone),
                noPeriodo(inicio, fim),
                pago(pago),
                recebido(recebido),
                aprovado(aprovado),
                doEmissor(emissorId)
        );
    }
}
