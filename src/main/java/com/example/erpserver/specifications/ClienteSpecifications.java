package com.example.erpserver.specifications;

import com.example.erpserver.entities.Cliente;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ClienteSpecifications {

    public static Specification<Cliente> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assinante").get("id"), ceoId);
    }

    public static Specification<Cliente> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null ? null : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<Cliente> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Cliente> comTelefone(String telefone) {
        return (root, query, criteriaBuilder) ->
                telefone == null ? null : criteriaBuilder.like(root.get("telefone"), telefone + "%");
    }

    public static Specification<Cliente> comFiltros(
            UUID ceoId,
            String cpf,
            String telefone,
            String nome
    ) {
        Specification<Cliente> spec = doCeo(ceoId);

        if (cpf != null) {
            spec = spec.and(comCpf(cpf));
        }

        if (nome != null) {
            spec = spec.and(comNome(nome));
        }

        if(telefone != null){
            spec = spec.and(comTelefone(telefone));
        }

        return spec;
    }
}