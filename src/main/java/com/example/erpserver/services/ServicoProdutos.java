package com.example.erpserver.services;

import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Produto;
import com.example.erpserver.DTOs.ProdutoDTO;
import com.example.erpserver.repositories.CeoRepositorio;
import com.example.erpserver.repositories.ProdutosRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.ProdutoSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ServicoProdutos {

    private final CeoRepositorio ceos;
    private final ProdutosRepositorio produtos;
    private final JwtUtil jwtUtil;

    public ServicoProdutos(
            ProdutosRepositorio produtos,
            JwtUtil jwtUtil,
            CeoRepositorio ceos
    ) {
        this.produtos = produtos;
        this.jwtUtil = jwtUtil;
        this.ceos = ceos;
    }

    // ---------- Adicionar Produto ----------
    @Transactional
    public Optional<Produto> adicionarProduto(String token, ProdutoDTO dto) {

        UUID ceoId = jwtUtil.extrairCeoId(token);

        Produto novoProduto = Produto.builder()
                .nome(dto.getNome())
                .preco(dto.getPreco())
                .estoqueDisponivel(dto.getQuantidade())
                .ceo(Ceo.builder().id(ceoId).build())
                .build();

        return Optional.of(produtos.save(novoProduto));
    }

    // ---------- Atualizar Produto ----------
    @Transactional
    public Optional<Produto> atualizarProduto(String token, UUID produtoId, ProdutoDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return produtos.findByCeoIdAndId(ceoId, produtoId)
                .map(p -> {
                    p.setNome(dto.getNome());
                    p.setPreco(dto.getPreco());

                    return produtos.save(p);
                });
    }

    // ---------- Movimentação de Estoque ----------
    // Helper para atualizar produto
    private Optional<Produto> atualizarProduto(String token, UUID produtoId, java.util.function.Consumer<Produto> atualizacao) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        return produtos.findByCeoIdAndId(ceoId, produtoId)
                .map(p -> {
                    atualizacao.accept(p);
                    return p; // não precisa chamar save() se está em @Transactional
                });
    }

    // ---------- Movimentação de Estoque ----------
    @Transactional
    public Optional<Produto> addEstoquePendente(String token, UUID produtoId, long quantidade) {
        return atualizarProduto(token, produtoId, p ->
                p.setEstoquePendente(p.getEstoquePendente() + quantidade)
        );
    }

    @Transactional
    public Optional<Produto> quitarEstoquePendente(String token, UUID produtoId, long quantidade) {
        return atualizarProduto(token, produtoId, p -> {

            if (p.getEstoquePendente() < quantidade) return;

            p.setEstoquePendente(p.getEstoquePendente() - quantidade);
            p.setEstoqueDisponivel(p.getEstoqueDisponivel() + quantidade);
        });
    }

    @Transactional
    public Optional<Produto> addEstoqueReservado(String token, UUID produtoId, long quantidade) {
        return atualizarProduto(token, produtoId, p -> {

            if (p.getEstoqueDisponivel() < quantidade) return;

            p.setEstoqueReservado(p.getEstoqueReservado() + quantidade);
            p.setEstoqueDisponivel(p.getEstoqueDisponivel() - quantidade);
        });
    }

    @Transactional
    public Optional<Produto> quitarEstoqueReservado(String token, UUID produtoId, long quantidade) {
        return atualizarProduto(token, produtoId, p -> {
            if (p.getEstoqueReservado() < quantidade) return;
            p.setEstoqueReservado(p.getEstoqueReservado() - quantidade);
        });
    }

    // ---------- Buscar Produtos (Paginação) ----------
    public PaginaDTO<Produto> buscarProdutos(
            String token,
            String nome,
            Boolean semEstoque,
            Boolean comEstoquePendente,
            Boolean comEstoqueReservado,
            int pagina,
            int tamanho
    ) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Produto> spec = ProdutoSpecifications.comFiltros(ceoId, nome, semEstoque, comEstoquePendente, comEstoqueReservado);

        return PaginaDTO.from(produtos.findAll(spec, pageable));
    }

    // ---------- Remover Produto ----------
    @Transactional
    public Optional<Produto> removerProduto(
            String token,
            UUID produtoId
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return produtos.findByCeoIdAndId(ceoId, produtoId)
                .map(produto -> {
                    produtos.delete(produto);
                    return produto;
                });
    }
}

