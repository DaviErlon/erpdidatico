package com.example.erpserver.specifications;

import com.example.erpserver.entities.Clientes;
import org.springframework.data.jpa.domain.Specification;

public class PessoaSpecifications {

    public static Specification<Clientes> doAssinante(Long assinanteId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assinante").get("id"), assinanteId);
    }

    public static Specification<Clientes> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null ? null : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<Clientes> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Clientes> isFornecedor(Boolean fornecedor) {
        return (root, query, criteriaBuilder) -> {
            if (fornecedor == null) return null;
            return fornecedor ?
                    criteriaBuilder.isTrue(root.get("fornecedor")) :
                    criteriaBuilder.isFalse(root.get("fornecedor"));
        };
    }

    public static Specification<Clientes> comFiltros(
            Long assinanteId,
            String cpf, String nome,
            Boolean fornecedor
    ) {
        Specification<Clientes> spec = doAssinante(assinanteId);

        // Adiciona filtros condicionalmente
        if (cpf != null && !cpf.isEmpty()) {
            spec = spec.and(comCpf(cpf));
        }

        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(comNome(nome));
        }

        if (fornecedor != null) {
            spec = spec.and(isFornecedor(fornecedor));
        }

        return spec;
    }
}