package com.example.erpserver.specifications;

import com.example.erpserver.entities.Fornecedor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class FornecedorSpecifications {

    public static Specification<Fornecedor> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ceo").get("id"), ceoId);
    }

    public static Specification<Fornecedor> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                (cpf == null || cpf.isEmpty()) ? null : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<Fornecedor> comCnpj(String cnpj) {
        return (root, query, criteriaBuilder) ->
                (cnpj == null || cnpj.isEmpty()) ? null : criteriaBuilder.like(root.get("cnpj"), cnpj + "%");
    }

    public static Specification<Fornecedor> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                (nome == null || nome.isEmpty()) ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Fornecedor> comTelefone(String telefone) {
        return (root, query, criteriaBuilder) ->
                (telefone == null || telefone.isEmpty()) ? null : criteriaBuilder.like(root.get("telefone"), telefone + "%");
    }

    public static Specification<Fornecedor> comFiltros(
            UUID ceoId,
            String cpf,
            String cnpj,
            String nome,
            String telefone
    ) {
        return Specification.allOf(
                doCeo(ceoId),
                comCpf(cpf),
                comCnpj(cnpj),
                comNome(nome),
                comTelefone(telefone)
        );
    }
}
