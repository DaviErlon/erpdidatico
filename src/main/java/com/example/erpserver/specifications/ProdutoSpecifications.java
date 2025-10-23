package com.example.erpserver.specifications;

import com.example.erpserver.entities.Produto;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProdutoSpecifications {

    public static Specification<Produto> doCeo(UUID ceoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ceo").get("id"), ceoId);
    }

    public static Specification<Produto> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                (nome == null || nome.isEmpty()) ? null :
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    public static Specification<Produto> semEstoqueFisico(Boolean aplicar) {
        return (root, query, criteriaBuilder) ->
                (aplicar != null && aplicar) ? criteriaBuilder.equal(root.get("estoqueDisponivel"), 0) : null;
    }

    public static Specification<Produto> comEstoquePendente(Boolean aplicar) {
        return (root, query, criteriaBuilder) ->
                (aplicar != null && aplicar) ? criteriaBuilder.notEqual(root.get("estoquePendente"), 0) : null;
    }

    public static Specification<Produto> comEstoqueReservado(Boolean aplicar) {
        return (root, query, criteriaBuilder) ->
                (aplicar != null && aplicar) ? criteriaBuilder.notEqual(root.get("estoqueReservado"), 0) : null;
    }

    public static Specification<Produto> comFiltros(
            UUID ceoId,
            String nome,
            Boolean semEstoqueFisico,
            Boolean comEstoquePendente,
            Boolean comEstoqueReservado
    ) {
        return Specification.allOf(
                doCeo(ceoId),
                comNome(nome),
                semEstoqueFisico(semEstoqueFisico),
                comEstoquePendente(comEstoquePendente),
                comEstoqueReservado(comEstoqueReservado)
        );
    }
}