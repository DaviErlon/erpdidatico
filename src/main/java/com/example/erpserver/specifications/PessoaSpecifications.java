package com.example.erpserver.specifications;

import com.example.erpserver.entities.Pessoa;
import org.springframework.data.jpa.domain.Specification;

public class PessoaSpecifications {

    public static Specification<Pessoa> doAssinante(Long assinanteId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assinante").get("id"), assinanteId);
    }

    public static Specification<Pessoa> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null ? null : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<Pessoa> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Pessoa> isFornecedor(Boolean fornecedor) {
        return (root, query, criteriaBuilder) -> {
            if (fornecedor == null) return null;
            return fornecedor ?
                    criteriaBuilder.isTrue(root.get("fornecedor")) :
                    criteriaBuilder.isFalse(root.get("fornecedor"));
        };
    }

    public static Specification<Pessoa> comFiltros(
            Long assinanteId,
            String cpf, String nome,
            Boolean fornecedor
    ) {
        Specification<Pessoa> spec = doAssinante(assinanteId);

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