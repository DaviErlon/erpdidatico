package com.example.erpserver.specifications;

import com.example.erpserver.entities.Fornecedor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class FornecedorSpecifications {

    public static Specification<Fornecedor> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assinante").get("id"), ceoId);
    }

    public static Specification<Fornecedor> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null ? null : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<Fornecedor> comTelefone(String telefone) {
        return (root, query, criteriaBuilder) ->
                telefone == null ? null : criteriaBuilder.like(root.get("telefone"), telefone + "%");
    }

    public static Specification<Fornecedor> comCnpj(String cnpj) {
        return (root, query, criteriaBuilder) ->
                cnpj == null || cnpj.isEmpty() ? null : criteriaBuilder.like(
                        root.get("cnpj"),
                        cnpj + "%"
                );
    }

    public static Specification<Fornecedor> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Fornecedor> comFiltros(
            UUID ceoId,
            String cpf,
            String cnpj,
            String telefone,
            String nome
    ) {
        Specification<Fornecedor> spec = doCeo(ceoId);

        if (cpf != null) {
            spec = spec.and(comCpf(cpf));
        }

        if (nome != null) {
            spec = spec.and(comNome(nome));
        }

        if (cnpj != null) {
            spec = spec.and(comCnpj(cnpj));
        }

        if (cnpj != null) {
            spec = spec.and(comTelefone(telefone));
        }

        return spec;
    }
}
