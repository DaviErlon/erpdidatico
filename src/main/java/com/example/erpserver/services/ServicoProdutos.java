package com.example.erpserver.services;

import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.Produto;
import com.example.erpserver.DTOs.ProdutoDTO;
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
import java.util.function.Consumer;

@Service
public class ServicoProdutos {

    private final ProdutosRepositorio produtos;
    private final JwtUtil jwtUtil;

    private final ServicoLogAuditoria servicoLogAuditoria;

    public ServicoProdutos(
            ProdutosRepositorio produtos,
            JwtUtil jwtUtil,
            ServicoLogAuditoria servicoLogAuditoria
    ) {
        this.servicoLogAuditoria = servicoLogAuditoria;
        this.produtos = produtos;
        this.jwtUtil = jwtUtil;
    }

    // ---------- Adicionar Produto ----------
    @Transactional
    public Optional<Produto> adicionarProduto(String token, ProdutoDTO dto) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        Ceo ceo = new Ceo();
        ceo.setId(ceoId);

        Funcionario emissor = new Funcionario();
        emissor.setId(funcionarioId);

        Produto novoProduto = new Produto();
        novoProduto.setNome(dto.getNome());
        novoProduto.setPreco(dto.getPreco());
        novoProduto.setEstoqueDisponivel(dto.getQuantidade());
        novoProduto.setCeo(ceo);

        novoProduto = produtos.save(novoProduto);

        servicoLogAuditoria.registrar(ceo, emissor, "CRIAÇÃO", "PRODUTO", novoProduto.getId(), "ADIÇÂO DE NOVO PRODUTO AO ESTOQUE");

        return Optional.of(novoProduto);
    }

    // ---------- Atualizar Produto ----------
    @Transactional
    public Optional<Produto> atualizarProduto(String token, UUID produtoId, ProdutoDTO dto) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return produtos.findByCeoIdAndId(ceoId, produtoId)
                .map(p -> {
                    p.setNome(dto.getNome());
                    p.setPreco(dto.getPreco());

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);

                    servicoLogAuditoria.registrar(ceo, emissor, "EDIÇÂO", "PRODUTO", p.getId(), "EDIÇÃO DE INFORMAÇÕES DO PRODUTO");

                    return produtos.save(p);
                });
    }

    // ---------- Movimentação de Estoque ----------
    private Optional<Produto> atualizarProduto(String token, UUID produtoId, Consumer<Produto> atualizacao) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        return produtos.findByCeoIdAndId(ceoId, produtoId)
                .map(p -> {
                    atualizacao.accept(p);
                    return p; // @Transactional garante persistência automática
                });
    }

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
    public Optional<Produto> removerProduto(String token, UUID produtoId) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return produtos.findByCeoIdAndId(ceoId, produtoId)
                .map(produto -> {
                    produtos.delete(produto);

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);

                    servicoLogAuditoria.registrar(ceo, emissor, "REMOÇÃO", "PRODUTO", produto.getId(), "REMOÇÃO TOTAL DO PRODUTO " + produto.getNome());

                    return produto;
                });
    }
}
