package com.example.erpserver.specifications;

import com.example.erpserver.entities.Clientes;
import com.example.erpserver.entities.Fornecedor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class FornecedorSpecifications {

    public static Specification<Fornecedor> doAssinante(UUID assinanteId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assinante").get("id"), assinanteId);
    }

    public static Specification<Fornecedor> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null ? null : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<Fornecedor> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Fornecedor> comFiltros(
            UUID assinanteId,
            String cpf,
            String nome
    ) {
        Specification<Fornecedor> spec = doAssinante(assinanteId);

        if (cpf != null && !cpf.isEmpty()) {
            spec = spec.and(comCpf(cpf));
        }

        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(comNome(nome));
        }

        return spec;
    }
}
