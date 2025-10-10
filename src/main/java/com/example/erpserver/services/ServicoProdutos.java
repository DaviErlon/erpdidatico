package com.example.erpserver.services;

import com.example.erpserver.entities.Produto;
import com.example.erpserver.DTOs.ProdutoDTO;
import com.example.erpserver.repository.AssinantesRepositorio;
import com.example.erpserver.repository.ProdutosRepositorio;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.ProdutoSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServicoProdutos {

    private final AssinantesRepositorio assinantesRepositorio;
    private final ProdutosRepositorio repositorio;
    private final JwtUtil jwtUtil;

    public ServicoProdutos(
            ProdutosRepositorio repositorio,
            JwtUtil jwtUtil,
            AssinantesRepositorio assinantesRepositorio
    ) {
        this.repositorio = repositorio;
        this.jwtUtil = jwtUtil;
        this.assinantesRepositorio = assinantesRepositorio;
    }

    // ---------- Adicionar Produto ----------
    @Transactional
    public Optional<Produto> addProduto(ProdutoDTO dto, String token) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return assinantesRepositorio.findById(assinanteId)
                .map(assinante -> {
                    Produto produto = new Produto();
                    produto.setNome(dto.getNome());
                    produto.setPreco(dto.getPreco());
                    produto.setEstoqueDisponivel(dto.getQuantidade());
                    produto.setEstoquePendente(0);
                    produto.setEstoqueReservado(0);
                    produto.setCeo(assinante);

                    return repositorio.save(produto);
                });
    }

    // ---------- Atualizar Produto ----------
    @Transactional
    public Optional<Produto> atualizarPorId(String token, Long produtoId, String nome, Double preco, int quantidade) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, produtoId)
                .map(produto -> {
                    produto.setNome(nome);
                    produto.setPreco(preco);
                    produto.setEstoqueDisponivel(quantidade);
                    return repositorio.save(produto);
                });
    }

    // ---------- Movimentação de Estoque ----------
    @Transactional
    public Optional<Produto> addEstoquePendente(String token, Long produtoId, int quantidade) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, produtoId)
                .map(produto -> {
                    produto.setEstoquePendente(produto.getEstoquePendente() + quantidade);
                    return repositorio.save(produto);
                });
    }

    @Transactional
    public Optional<Produto> quitarEstoquePendente(String token, Long produtoId, int quantidade) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, produtoId)
                .filter(produto -> produto.getEstoquePendente() >= quantidade)
                .map(produto -> {
                    produto.setEstoquePendente(produto.getEstoquePendente() - quantidade);
                    produto.setEstoqueDisponivel(produto.getEstoqueDisponivel() + quantidade);
                    return repositorio.save(produto);
                });
    }

    @Transactional
    public Optional<Produto> addEstoqueReservado(String token, Long produtoId, int quantidade) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, produtoId)
                .filter(produto -> produto.getEstoqueDisponivel() >= quantidade)
                .map(produto -> {
                    produto.setEstoqueReservado(produto.getEstoqueReservado() + quantidade);
                    produto.setEstoqueDisponivel(produto.getEstoqueDisponivel() - quantidade);

                    return repositorio.save(produto);
                });
    }

    @Transactional
    public Optional<Produto> quitarEstoqueReservado(String token, Long produtoId, int quantidade) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, produtoId)
                .filter(produto -> produto.getEstoqueReservado() >= quantidade)
                .map(produto -> {
                    produto.setEstoqueReservado(produto.getEstoqueReservado() - quantidade);
                    return repositorio.save(produto);
                });
    }

    // ---------- Buscar Produtos (Paginação) ----------
    public Page<Produto> buscarProdutos(
            String token,
            String nome,
            Boolean semEstoque,
            Boolean comEstoquePendente,
            Boolean comEstoqueReservado,
            int pagina,
            int tamanho
    ) {

        Long assinanteId = jwtUtil.extrairAdminId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Produto> spec = ProdutoSpecifications.comFiltros(assinanteId, nome, semEstoque, comEstoquePendente, comEstoqueReservado);

        return repositorio.findAll(spec, pageable);
    }


    // ---------- Remover Produto ----------
    @Transactional
    public Optional<Produto> removerPorId(String token, Long produtoId) {
        Long assinanteId = jwtUtil.extrairAdminId(token);

        return repositorio.findByAssinanteIdAndId(assinanteId, produtoId)
                .map(produto -> {
                    repositorio.delete(produto);
                    return produto;
                });
    }
}

