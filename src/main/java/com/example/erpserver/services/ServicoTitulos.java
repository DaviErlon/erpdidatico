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
    private final JwtUtil jwtUtil;
    private final ServicoProdutos servicoProdutos;
    private final ServicoEmail servicoEmail;

    private final ServicoLogAuditoria servicoLogAuditoria;

    public ServicoTitulos(
            TitulosRepositorio titulos,
            ProdutosDosTitulosRepositorio produtosDosTitulos,
            ProdutosRepositorio produtos,
            ClientesRepositorio clientes,
            FornecedoresRepositorio fornecedores,
            FuncionariosRepositorio funcionarios,
            ServicoProdutos servicoProdutos,
            ServicoEmail servicoEmail,
            JwtUtil jwtUtil,
            ServicoLogAuditoria servicoLogAuditoria
    ) {
        this.servicoLogAuditoria = servicoLogAuditoria;
        this.titulos = titulos;
        this.produtosDosTitulos = produtosDosTitulos;
        this.produtos = produtos;
        this.clientes = clientes;
        this.fornecedores = fornecedores;
        this.funcionarios = funcionarios;
        this.servicoProdutos = servicoProdutos;
        this.servicoEmail = servicoEmail;
        this.jwtUtil = jwtUtil;
    }

    // ---------- Criar Título Cliente ----------
    @Transactional
    public Optional<Titulo> adicionarTituloCliente(String token, TituloDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        return funcionarios.findByCeoIdAndId(ceoId, emissorId)
                .flatMap(emissor -> {
                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);
                    
                    Cliente cliente = clientes.findByCeoIdAndId(ceoId, dto.getId()).orElse(null);
                    
                    Optional<Titulo> response = criarTituloCliente(ceo, cliente, dto, token, emissor);
                    
                    if(response.isPresent()){
                        servicoLogAuditoria.registrar(ceo, emissor, "EMISSÃO", "TÍTULO", response.get().getId(), "TÍTULO A RECEBER DE CLIENTE"); 
                    }
                    return response;
                });
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

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

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
                        if (produto == null) throw new IllegalStateException("Produto não encontrado");

                        BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
                        total = total.add(subtotal);

                        ProdutosDosTitulos pdt = new ProdutosDosTitulos();
                        pdt.setNome(produto.getNome());
                        pdt.setValor(produto.getPreco());
                        pdt.setQuantidade(item.getQuantidade());
                        pdt.setProduto(produto);
                        produtosTitulo.add(pdt);
                    }

                    Titulo titulo = new Titulo();
                    titulo.setCeo(ceo);
                    titulo.setFornecedor(fornecedor);
                    titulo.setCpf(fornecedor.getCpf());
                    titulo.setCnpj(fornecedor.getCnpj());
                    titulo.setTelefone(fornecedor.getTelefone());
                    titulo.setValor(total.negate());
                    titulo.setProdutosDosTitulos(produtosTitulo);
                    titulo.setEmissor(emissor);

                    titulo = titulos.save(titulo);

                    servicoLogAuditoria.registrar(ceo, emissor, "EMISSÃO", "TÍTULO", titulo.getId(), "TÍTULO A PAGAR FORNECEDOR");

                    return Optional.of(titulo);
                }).orElse(Optional.empty());
    }

    // ---------- Criar Título para Funcionário ----------
    @Transactional
    public Optional<Titulo> adicionarTituloFuncionario(String token, UUID funcionarioId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        return funcionarios.findByCeoIdAndId(ceoId, emissorId)
                .map(emissor -> {
                    Optional<Funcionario> funcionarioOpt = funcionarios.findByCeoIdAndId(ceoId, funcionarioId);
                    if (funcionarioOpt.isEmpty()) return Optional.<Titulo>empty();

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario funcionario = funcionarioOpt.get();

                    Titulo titulo = new Titulo();
                    titulo.setCeo(ceo);
                    titulo.setFuncionario(funcionario);
                    titulo.setNome(funcionario.getNome());
                    titulo.setCpf(funcionario.getCpf());
                    titulo.setTelefone(funcionario.getTelefone());
                    titulo.setValor(funcionario.getSalario().negate());
                    titulo.setEmissor(emissor);

                    titulo = titulos.save(titulo);

                    servicoLogAuditoria.registrar(ceo, emissor, "EMISSÃO", "TÍTULO", titulo.getId(), "TÍTULO A PAGAR FUNCIONARIO");

                    return Optional.of(titulo);
                }).orElse(Optional.empty());
    }

    @Transactional
    public Optional<Titulo> aprovarTitulo(UUID tituloId, String token) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .map(titulo -> {
                    aprovar(titulo, token);

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);

                    servicoLogAuditoria.registrar(ceo, emissor, "APROVAR", "TÍTULO", titulo.getId(), "TÍTULO APROVADO");

                    return titulos.save(titulo);
                });
    }

    // ---------- Marcar como Pago ----------
    @Transactional
    public Optional<Titulo> pagarTitulo(String token, UUID tituloId) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return titulos.findByCeoIdAndIdAndPagoFalseAndAprovadoTrue(ceoId, tituloId)
                .map(titulo -> {
                    BigDecimal valor = titulo.getValor();
                    boolean dentroDoIntervalo = valor.compareTo(BigDecimal.ZERO) > 0
                            && valor.compareTo(BigDecimal.valueOf(10000)) < 0;

                    if (dentroDoIntervalo && !titulo.isEstoqueMovimentado() && titulo.getFuncionario() == null) {
                        movimentarEstoqueTitulo(token, tituloId);
                    }

                    titulo.setPago(true);

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);
                    
                    servicoLogAuditoria.registrar(ceo, emissor, "PAGAR", "TÍTULO", titulo.getId(), "TÍTULO PAGO");

                    return titulos.save(titulo);
                });
    }

    // ---------- Confirmar movimentação estoque ----------
    @Transactional
    public Optional<Titulo> movimentarEstoqueTitulo(String token, UUID tituloId) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return titulos.findByCeoIdAndIdAndEstoqueMovimentadoFalseAndFuncionarioIsNullAndAprovadoTrue(ceoId, tituloId)
                .map(titulo -> {
                    List<ProdutosDosTitulos> itens = produtosDosTitulos.findByTituloId(titulo.getId());
                    boolean sign = titulo.getValor().compareTo(BigDecimal.ZERO) > 0;

                    for (ProdutosDosTitulos item : itens) {
                        Produto produto = item.getProduto();
                        long quantidade = item.getQuantidade();

                        if (sign) {
                            servicoProdutos.quitarEstoqueReservado(token, produto.getId(), quantidade);
                        } else {
                            servicoProdutos.quitarEstoquePendente(token, produto.getId(), quantidade);
                        }
                    }
                    
                    titulo.setEstoqueMovimentado(true);

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);
                    
                    servicoLogAuditoria.registrar(ceo, emissor, "MOVIMENTO DE ESTOQUE", "TÍTULO", titulo.getId(), "TÍTULO ATUALIZOU O ESTOQUE DE UM OU MAIS PRODUTOS");
                    
                    return titulos.save(titulo);
                });
    }

    // ---------- Buscar Titulos ----------
    public PaginaDTO<Titulo> buscarTitulos(
            String token,
            String cpf,
            String cnpj,
            String nome,
            String telefone,
            LocalDateTime inicio,
            LocalDateTime fim,
            Boolean pago,
            Boolean recebido,
            Boolean aprovado,
            int pagina,
            int tamanho
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Titulo> spec = TituloSpecifications.comFiltros(ceoId, cpf, cnpj, nome, telefone, inicio, fim, pago, recebido, aprovado, null);
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
            int tamanho
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Titulo> spec = TituloSpecifications.comFiltros(ceoId, cpf, cnpj, nome, null, inicio, fim, pago, recebido, aprovado, emissorId);
        return PaginaDTO.from(titulos.findAll(spec, pageable));
    }

    // ---------- Remover Título ----------
    @Transactional
    public Optional<Titulo> removerTitulo(String token, UUID tituloId) {
        
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .map(titulo -> {
                    if (!titulo.isPago()) {
                        if (titulo.getFuncionario() == null) {
                            List<ProdutosDosTitulos> itens = produtosDosTitulos.findByTituloId(titulo.getId());
                            for (ProdutosDosTitulos item : itens) {
                                Produto produto = item.getProduto();
                                long quantidade = item.getQuantidade();

                                if (titulo.getValor().compareTo(BigDecimal.ZERO) < 0) {
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

                                        Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);
                    
                    servicoLogAuditoria.registrar(ceo, emissor, "DELETAR", "TÍTULO", titulo.getId(), "TÍTULO REMOVIDO DO SISTEMA");

                    return titulo;
                });
    }

    @Transactional
    private void aprovar(Titulo titulo, String token) {
        var produtosTitulo = titulo.getProdutosDosTitulos();

        if (produtosTitulo != null && !titulo.isAprovado()) {
            if (titulo.getValor().compareTo(BigDecimal.ZERO) < 0) {
                for (ProdutosDosTitulos pdt : produtosTitulo) {
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

    // ---------- Método auxiliar ----------
    private Optional<Titulo> criarTituloCliente(Ceo ceo, Cliente cliente, TituloDTO dto, String token, Funcionario emissor) {
        BigDecimal total = BigDecimal.ZERO;
        Set<ProdutosDosTitulos> produtosTitulo = new HashSet<>();

        for (ItemProdutoDTO item : dto.getProdutos()) {
            var produtoOpt = produtos.findByCeoIdAndId(ceo.getId(), item.getProdutoId());
            if (produtoOpt.isEmpty()) throw new IllegalStateException("Produto não encontrado");

            var produto = produtoOpt.get();
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotal);

            ProdutosDosTitulos pdt = new ProdutosDosTitulos();
            pdt.setNome(produto.getNome());
            pdt.setValor(produto.getPreco());
            pdt.setQuantidade(item.getQuantidade());
            pdt.setProduto(produto);
            produtosTitulo.add(pdt);
        }

        if (total.compareTo(BigDecimal.valueOf(10000)) > 0) {
            for (var f : funcionarios.findAllByCeoId(ceo.getId()).stream()
                    .filter(f -> f.getTipo().equals(TipoEspecializacao.GESTOR))
                    .toList()) {
                servicoEmail.enviarEmail(
                        f.getEmail(),
                        "Alerta: título elevado",
                        "Um novo título de valor de " + total + "R$ foi emitido para o cliente: " + cliente.getNome()
                                + "\nTítulo registrado pelo funcionario: " + emissor.getNome()
                                + "\nE-mail do funcionário:" + emissor.getEmail()
                );
            }
        }

        boolean aprovado = total.compareTo(BigDecimal.valueOf(50000)) >= 0;

        Titulo titulo = new Titulo();
        titulo.setCeo(ceo);
        titulo.setCliente(cliente);
        titulo.setValor(total);
        titulo.setCpf(cliente != null ? cliente.getCpf() : null);
        titulo.setNome(cliente != null ? cliente.getNome() : "ClientePDV");
        titulo.setTelefone(cliente != null ? cliente.getTelefone() : null);
        titulo.setProdutosDosTitulos(produtosTitulo);
        titulo.setEmissor(emissor);

        if (aprovado) {
            aprovar(titulo, token);
        }

        return Optional.of(titulos.save(titulo));
    }
}
