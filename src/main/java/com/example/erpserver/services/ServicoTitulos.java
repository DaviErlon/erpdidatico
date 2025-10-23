package com.example.erpserver.services;

import com.example.erpserver.DTOs.ItemProdutoDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.DTOs.TituloDTO;
import com.example.erpserver.entities.*;
import com.example.erpserver.repositories.*;
import com.example.erpserver.security.JwtUtil;
import com.example.erpserver.specifications.TituloSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ServicoEmail servicoEmail;

    public ServicoTitulos(
            TitulosRepositorio titulos,
            ProdutosDosTitulosRepositorio produtosDosTitulos,
            ProdutosRepositorio produtos,
            ClientesRepositorio clientes,
            FornecedoresRepositorio fornecedores,
            FuncionariosRepositorio funcionarios,
            CeoRepositorio ceos,
            ServicoProdutos servicoProdutos,
            ServicoEmail servicoEmail,
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
        this.servicoEmail = servicoEmail;
        this.jwtUtil = jwtUtil;
    }

    // ---------- Criar Título Cliente ----------
    @Transactional
    public Optional<Titulo> addTituloCliente(String token, TituloDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return funcionarios.findByCeoIdAndId(ceoId, funcionarioId)
                .flatMap(emissor ->
                        ceos.findById(ceoId)
                                .flatMap(ceo ->
                                        clientes.findByCeoIdAndId(ceoId, dto.getId())
                                                .flatMap(cliente ->
                                                        criarTituloCliente(ceo, cliente, dto, token, emissor)
                                                )
                                                .or(() ->
                                                        criarTituloCliente(ceo, null, dto, token, emissor)
                                                )
                                )
                );
    }

    // ---------- Método auxiliar para criar o título ----------
    private Optional<Titulo> criarTituloCliente(Ceo ceo, Cliente cliente, TituloDTO dto, String token, Funcionario emissor) {

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

            for(var f : funcionarios.findAllByCeoId(ceo.getId()).stream().filter(f -> f.getTipo().equals(TipoEspecializacao.GESTOR)).toList()){
                servicoEmail.enviarEmail(
                        f.getEmail(),
                        "Alerta: título elevado",
                        "Um novo título de valor de " + total + "R$ foi emitido para o cliente: " + cliente.getNome()
                        + "\nTítulo registrado pelo funcionario: " + emissor.getNome()
                        + "\nE-mail do funcionário:" + emissor.getEmail()
                );
            }
        }

        boolean aprovado = total.compareTo(new BigDecimal("50000.00")) >= 0;

        Titulo titulo = Titulo.builder()
                .ceo(ceo)
                .cliente(cliente)
                .valor(total)
                .cpf(cliente != null ? cliente.getCpf() : null)
                .nome(cliente != null ? cliente.getNome() : "ClientePDV")
                .telefone(cliente != null ? cliente.getTelefone() : null)
                .produtosDosTitulos(produtosTitulo)
                .emissor(emissor)
                .build();

        if(aprovado){
            aprovar(titulo, token);
        }

        return Optional.of(titulos.save(titulo));
    }

    // ---------- Criar Título Fornecedor ----------
    @Transactional
    public Optional<Titulo> adicionarTituloFornecedor(String token, TituloDTO dto) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        return funcionarios.findByCeoIdAndId(ceoId, emissorId)
                .map(emissor -> {

                    var fornecedorOpt = fornecedores.findByCeoIdAndId(ceoId, dto.getId());
                    if (fornecedorOpt.isEmpty()) return Optional.<Titulo>empty();

                    var fornecedor = fornecedorOpt.get();
                    Ceo ceo = Ceo.builder().id(ceoId).build();

                    var produtoIds = dto.getProdutos().stream()
                            .map(ItemProdutoDTO::getProdutoId)
                            .toList();

                    var produtosMap = produtos.findAllByCeoIdAndIdIn(ceoId, produtoIds)
                            .stream()
                            .collect(Collectors.toMap(Produto::getId, p -> p));

                    Set<ProdutosDosTitulos> produtosTitulo = new HashSet<>();
                    BigDecimal total = BigDecimal.ZERO;

                    for (ItemProdutoDTO item : dto.getProdutos()) {
                        Produto produto = produtosMap.get(item.getProdutoId());
                        if (produto == null) return Optional.<Titulo>empty();

                        BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
                        total = total.add(subtotal);

                        produtosTitulo.add(ProdutosDosTitulos.builder()
                                .nome(produto.getNome())
                                .valor(produto.getPreco())
                                .quantidade(item.getQuantidade())
                                .produto(produto)
                                .build()
                        );
                    }

                    Titulo titulo = Titulo.builder()
                            .ceo(ceo)
                            .fornecedor(fornecedor)
                            .cpf(fornecedor.getCpf())
                            .cnpj(fornecedor.getCnpj())
                            .telefone(fornecedor.getTelefone())
                            .valor(total.negate())
                            .produtosDosTitulos(produtosTitulo)
                            .emissor(emissor)
                            .build();

                    aprovar(titulo, token);
                    return Optional.of(titulos.save(titulo));
                }).orElse(Optional.empty());
    }

    // ---------- Criar Título para Funcionário ----------
    @Transactional
    public Optional<Titulo> addTituloFuncionario(String token, UUID funcionarioId) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        return funcionarios.findByCeoIdAndId(ceoId, emissorId)
                .map(emissor -> {

                    Optional<Funcionario> funcionarioOpt = funcionarios.findByCeoIdAndId(ceoId, funcionarioId);

                    if (funcionarioOpt.isEmpty()) return Optional.<Titulo>empty();

                    var ceo = Ceo.builder().id(ceoId).build();
                    var titulo = Titulo.builder()
                            .ceo(ceo)
                            .funcionario(funcionarioOpt.get())
                            .nome(funcionarioOpt.get().getNome())
                            .cpf(funcionarioOpt.get().getCpf())
                            .telefone(funcionarioOpt.get().getTelefone())
                            .valor(funcionarioOpt.get().getSalario().negate())
                            .emissor(emissor)
                            .build();

                    return Optional.of(titulos.save(titulo));
                }).orElse(Optional.empty());
    }

    @Transactional
    public Optional<Titulo> aprovarTitulo(UUID tituloId, String token) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .map(titulo -> {
                    aprovar(titulo, token);
                    return titulos.save(titulo);
                });
    }

    // ---------- Marcar como Pago ----------
    @Transactional
    public Optional<Titulo> pagarTitulo(String token, UUID tituloId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return titulos.findByCeoIdAndIdAndPagoFalseAndAprovadoTrue(ceoId, tituloId)
                .map(titulo -> {
                    titulo.setPago(true);
                    return titulos.save(titulo);
                });
    }

    // ---------- Confirmar movimentação estoque como Pago ----------
    @Transactional
    public Optional<Titulo> receberEstoqueTitulo(String token, UUID tituloId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        return titulos.findByCeoIdAndIdAndRecebidoNoEstoqueFalseAndFuncionarioIsNullAndAprovadoTrueAndPagoTrueAndValorLessThan(ceoId, tituloId, BigDecimal.ZERO)
                .map(titulo -> {

                    List<ProdutosDosTitulos> itens = produtosDosTitulos.findByTituloId(titulo.getId());

                    boolean pagarOuReceber = titulo.getValor().compareTo(new BigDecimal("0")) < 0;

                    for (ProdutosDosTitulos item : itens) {

                        Produto produto = item.getProduto();
                        long quantidade = item.getQuantidade();

                        if (pagarOuReceber) {
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
            Boolean aprovado,
            int pagina,
            int tamanho) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Titulo> spec = TituloSpecifications.comFiltros(ceoId, cpf, cnpj, nome, inicio, fim, pago, recebido, aprovado, null);

        return PaginaDTO.from(titulos.findAll(spec, pageable));
    }

    public PaginaDTO<Titulo> buscarTitulosEmitidos(
            String token,
            String cpf,
            String cnpj,
            String nome,
            LocalDateTime inicio,
            LocalDateTime fim,
            Boolean pago,
            Boolean recebido,
            Boolean aprovado,
            int pagina,
            int tamanho) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Titulo> spec = TituloSpecifications.comFiltros(ceoId, cpf, cnpj, nome, inicio, fim, pago, recebido, aprovado, emissorId);

        return PaginaDTO.from(titulos.findAll(spec, pageable));
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

    @Transactional
    private void aprovar(Titulo titulo, String token){

        var produtosTitulo = titulo.getProdutosDosTitulos();

        if(produtosTitulo != null && !titulo.isAprovado()){
            if(titulo.getValor().compareTo(new BigDecimal("0")) < 0){
                for(ProdutosDosTitulos pdt : produtosTitulo){
                    pdt.setTitulo(titulo);
                    servicoProdutos.addEstoquePendente(token, pdt.getProduto().getId(), pdt.getQuantidade());
                }
            } else {
                for (ProdutosDosTitulos pdt : produtosTitulo) {
                    pdt.setTitulo(titulo);
                    servicoProdutos.addEstoqueReservado(token, pdt.getProduto().getId(), pdt.getQuantidade());
                }
            }
        }
        titulo.setAprovado(true);
    }
}

