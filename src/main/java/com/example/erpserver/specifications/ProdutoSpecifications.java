package com.example.erpserver.specifications;

import com.example.erpserver.entities.Produto;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProdutoSpecifications {

    public static Specification<Produto> doCeo(UUID assinanteId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assinante").get("id"), assinanteId);
    }

    public static Specification<Produto> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                nome == null || nome.isEmpty() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                );
    }

    public static Specification<Produto> semEstoqueFisico() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("estoqueDisponivel"), 0);
    }

    public static Specification<Produto> comEstoquePendente() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("estoquePendente"), 0);
    }

    public static Specification<Produto> comEstoqueReservado() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("estoqueReservado"), 0);
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
                semEstoqueFisico != null && semEstoqueFisico ? semEstoqueFisico() : null,
                comEstoquePendente != null && comEstoquePendente ? comEstoquePendente() : null,
                comEstoqueReservado != null && comEstoqueReservado ? comEstoqueReservado() : null
        );
    }
}