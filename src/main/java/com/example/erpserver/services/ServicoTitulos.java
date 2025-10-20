package com.example.erpserver.services;

import com.example.erpserver.DTOs.ItemProdutoDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.DTOs.TituloDTO;
import com.example.erpserver.entities.*;
import com.example.erpserver.repository.*;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.TituloSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ServicoTitulos {

    private final TitulosRepositorio titulos;
    private final ProdutosDosTitulosRepositorio produtosDosTitulos;
    private final ProdutosRepositorio produtos;
    private final ClientesRepositorio clientes;
    private final FornecedoresRepositorio fornecedores;
    private final FuncionariosRepositorio funcionarios;
    private final CeoRepositorio ceos;
    private final JwtUtil jwtUtil;
    private final ServicoProdutos servicoProdutos;

    public ServicoTitulos(
            TitulosRepositorio titulos,
            ProdutosDosTitulosRepositorio produtosDosTitulos,
            ProdutosRepositorio produtos,
            ClientesRepositorio clientes,
            FornecedoresRepositorio fornecedores,
            FuncionariosRepositorio funcionarios,
            CeoRepositorio ceos,
            ServicoProdutos servicoProdutos,
            JwtUtil jwtUtil
    ) {
        this.titulos = titulos;
        this.produtosDosTitulos = produtosDosTitulos;
        this.produtos = produtos;
        this.clientes = clientes;
        this.fornecedores = fornecedores;
        this.funcionarios = funcionarios;
        this.ceos = ceos;
        this.servicoProdutos = servicoProdutos;
        this.jwtUtil = jwtUtil;
    }

    // ---------- Criar Título Cliente ----------
    @Transactional
    public Optional<Titulo> addTituloCliente(String token, TituloDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return ceos.findById(ceoId).flatMap(ceo ->
                clientes.findByCeoIdAndId(ceoId, dto.getId())
                        .flatMap(cliente -> criarTituloCliente(ceo, cliente, dto, token))
                        .or(() -> criarTituloCliente(ceo, null, dto, token))
        );
    }

    // ---------- Método auxiliar para criar o título ----------
    private Optional<Titulo> criarTituloCliente(Ceo ceo, Cliente cliente, TituloDTO dto, String token) {

        BigDecimal total = BigDecimal.ZERO;
        Set<ProdutosDosTitulos> produtosTitulo = new HashSet<>();

        for (ItemProdutoDTO item : dto.getProdutos()) {
            var produtoOpt = produtos.findByCeoIdAndId(ceo.getId(), item.getProdutoId());
            if (produtoOpt.isEmpty()) {
                // se algum produto não pertence ao CEO, aborta a operação
                return Optional.empty();
            }

            var produto = produtoOpt.get();
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotal);

            ProdutosDosTitulos pdt = ProdutosDosTitulos.builder()
                    .nome(produto.getNome())
                    .valor(produto.getPreco())
                    .quantidade(item.getQuantidade())
                    .produto(produto)
                    .build();

            produtosTitulo.add(pdt);
        }

        if (total.compareTo(new BigDecimal("10000.00")) > 0) {
            // enviar e-mail (opcional)
        }

        Titulo titulo = Titulo.builder()
                .ceo(ceo)
                .cliente(cliente)
                .valor(total)
                .cpf(cliente != null ? cliente.getCpf() : null)
                .nome(cliente != null ? cliente.getNome() : "ClientePDV")
                .telefone(cliente != null ? cliente.getTelefone() : null)
                .produtosDosTitulos(produtosTitulo)
                .build();

        // Atualiza estoque (reserva)
        for (ProdutosDosTitulos pdt : produtosTitulo) {
            pdt.setTitulo(titulo);
            servicoProdutos.addEstoqueReservado(token, pdt.getProduto().getId(), pdt.getQuantidade());
        }

        return Optional.of(titulos.save(titulo));
    }


    // ---------- Criar Título Fornecedor ----------
    @Transactional
    public Optional<Titulo> addTituloFornecedor(String token, TituloDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return ceos.findById(ceoId).flatMap(ceo ->
                fornecedores.findByCeoIdAndId(ceoId, dto.getId())
                        .flatMap(fornecedor -> {

                            BigDecimal total = BigDecimal.ZERO;
                            Set<ProdutosDosTitulos> produtosTitulo = new HashSet<>();

                            for (ItemProdutoDTO item : dto.getProdutos()) {
                                var produtoOpt = produtos.findByCeoIdAndId(ceoId, item.getProdutoId());
                                if (produtoOpt.isEmpty()) {
                                    // Se algum produto não pertencer ao CEO, aborta toda a operação
                                    return Optional.empty();
                                }

                                var produto = produtoOpt.get();
                                BigDecimal subtotal = produto.getPreco()
                                        .multiply(BigDecimal.valueOf(item.getQuantidade()));
                                total = total.add(subtotal);

                                ProdutosDosTitulos pdt = ProdutosDosTitulos.builder()
                                        .nome(produto.getNome())
                                        .valor(produto.getPreco())
                                        .quantidade(item.getQuantidade())
                                        .produto(produto)
                                        .build();

                                produtosTitulo.add(pdt);
                            }

                            // Criação do título (negativo = a pagar)
                            Titulo titulo = Titulo.builder()
                                    .ceo(ceo)
                                    .fornecedor(fornecedor)
                                    .cpf(fornecedor.getCpf())
                                    .cnpj(fornecedor.getCnpj())
                                    .telefone(fornecedor.getTelefone())
                                    .valor(total.negate())
                                    .produtosDosTitulos(produtosTitulo)
                                    .build();

                            // Vincula os produtos ao título e ajusta o estoque
                            for (ProdutosDosTitulos pdt : produtosTitulo) {
                                pdt.setTitulo(titulo);
                                servicoProdutos.addEstoquePendente(
                                        token,
                                        pdt.getProduto().getId(),
                                        pdt.getQuantidade()
                                );
                            }

                            return Optional.of(titulos.save(titulo));
                        })
        );
    }


    // ---------- Criar Título para Funcionário ----------
    @Transactional
    public Optional<Titulo> addTituloFuncionario(String token, UUID funcionarioId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return ceos.findById(ceoId)
                .flatMap(ceo -> funcionarios.findByCeoIdAndId(ceoId, funcionarioId)
                            .map(funcionario -> {
                                BigDecimal salario = funcionario.getSalario();

                                Titulo titulo = Titulo.builder()
                                        .ceo(ceo)
                                        .funcionario(funcionario)
                                        .nome(funcionario.getNome())
                                        .cpf(funcionario.getCpf())
                                        .telefone(funcionario.getTelefone())
                                        .valor(salario.negate())
                                        .build();

                                return titulos.save(titulo);
                            })
                );
    }

    // ---------- Remover Título ----------
    @Transactional
    public Optional<Titulo> removerTitulo(String token, UUID tituloId) {

        UUID ceoId = jwtUtil.extrairCeoId(token);

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .map(titulo -> {

                    if (!titulo.isPago()) {
                        if(titulo.getFuncionario() == null){

                            List<ProdutosDosTitulos> itens = produtosDosTitulos.findByTituloId(titulo.getId());

                            for (ProdutosDosTitulos item : itens) {
                                Produto produto = item.getProduto();
                                long quantidade = item.getQuantidade();

                                if (titulo.getValor().compareTo(new BigDecimal("0")) < 0) {
                                    produto.setEstoquePendente(produto.getEstoquePendente() - quantidade);
                                } else {
                                    produto.setEstoqueReservado(produto.getEstoqueReservado() - quantidade);
                                    produto.setEstoqueDisponivel(produto.getEstoqueDisponivel() + quantidade);
                                }

                                produtos.save(produto);
                            }
                        }
                    }

                    titulos.delete(titulo);
                    return titulo;
                });
    }

    // ---------- Marcar como Pago ----------
    @Transactional
    public Optional<Titulo> quitarTitulo(String token, UUID tituloId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .filter(titulo -> !titulo.isPago())
                .map(titulo -> {

                    titulo.setPago(true);
                    return titulos.save(titulo);
                });
    }

    // ---------- Confirmar movimentação estoque como Pago ----------
    @Transactional
    public Optional<Titulo> confEstoqueTitulo(String token, UUID tituloId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .filter(titulo -> !titulo.isRecebidoNoEstoque() && titulo.getFuncionario() == null)
                .map(titulo -> {

                    List<ProdutosDosTitulos> itens = produtosDosTitulos.findByTituloId(titulo.getId());

                    boolean isFornecedor = titulo.getValor().compareTo(new BigDecimal("0")) < 0;

                    for (ProdutosDosTitulos item : itens) {

                        Produto produto = item.getProduto();
                        long quantidade = item.getQuantidade();

                        if (isFornecedor) {
                            servicoProdutos.quitarEstoquePendente(token, produto.getId(), quantidade);
                        } else {
                            servicoProdutos.quitarEstoqueReservado(token, produto.getId(), quantidade);
                        }
                    }

                    titulo.setRecebidoNoEstoque(true);
                    return titulos.save(titulo);
                });
    }

    // ---------- Buscar Titulos (Paginação) ----------
    public PaginaDTO<Titulo> buscarTitulos(
            String token,
            String cpf,
            String cnpj,
            String nome,
            LocalDateTime inicio,
            LocalDateTime fim,
            Boolean pago,
            Boolean recebido,
            int pagina,
            int tamanho) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Titulo> spec = TituloSpecifications.comFiltros(ceoId, cpf, cnpj, nome, inicio, fim, pago, recebido);

        return PaginaDTO.from(titulos.findAll(spec, pageable));
    }

}

