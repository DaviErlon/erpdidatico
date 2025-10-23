package com.example.erpserver.specifications;

import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.TipoEspecializacao;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class FuncionarioSpecifications {

    public static Specification<Funcionario> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ceo").get("id"), ceoId);
    }

    public static Specification<Funcionario> comTelefone(String telefone) {
        return (root, query, criteriaBuilder) ->
                (telefone == null || telefone.isEmpty()) ? null : criteriaBuilder.like(root.get("telefone"), telefone + "%");
    }

    public static Specification<Funcionario> comCpf(String cpf) {
        return (root, query, criteriaBuilder) ->
                (cpf == null || cpf.isEmpty()) ? null : criteriaBuilder.like(root.get("cpf"), cpf + "%");
    }

    public static Specification<Funcionario> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                (nome == null || nome.isEmpty()) ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Funcionario> comEspecializacao(TipoEspecializacao tipo) {
        return (root, query, criteriaBuilder) ->
                tipo == null ? null : criteriaBuilder.equal(root.get("tipo"), tipo);
    }

    public static Specification<Funcionario> comFiltros(
            UUID ceoId,
            String cpf,
            String nome,
            String telefone,
            TipoEspecializacao tipo
    ) {
        return Specification.allOf(
                doCeo(ceoId),
                comCpf(cpf),
                comNome(nome),
                comTelefone(telefone),
                comEspecializacao(tipo)
        );
    }
}
