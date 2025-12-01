package com.example.erpserver.services;

import com.example.erpserver.DTOs.ItemProdutoDTO;
import com.example.erpserver.DTOs.PaginaDTO;
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
    public Optional<Titulo> criarVendaCliente(String token, UUID clienteId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        return funcionarios.findByCeoIdAndId(ceoId, emissorId)
                .flatMap(emissor -> {

                    if(titulos.existsByEmissorIdAndAprovadoFalse(emissorId)){
                        return titulos.findFirstByEmissorIdAndPagoFalseOrderByIdAsc(emissorId);
                    }

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Cliente cliente = clientes.findByCeoIdAndId(ceoId, clienteId).orElse(null);

                    Titulo titulo = new Titulo();
                    titulo.setCeo(ceo);
                    titulo.setCliente(cliente);
                    titulo.setEmissor(emissor);
                    titulo.setProdutosDosTitulos(new HashSet<>());
                    titulo.setValor(BigDecimal.ZERO);
                    titulo.setCpf(cliente != null ? cliente.getCpf() : null);
                    titulo.setNome(cliente != null ? cliente.getNome() : "ClientePDV");
                    titulo.setTelefone(cliente != null ? cliente.getTelefone() : null);

                    Titulo salvo = titulos.save(titulo);

                    servicoLogAuditoria.registrar(
                            ceo, emissor, "EMISSÃO", "TÍTULO", salvo.getId(),
                            "TÍTULO A RECEBER DE CLIENTE"
                    );

                    return Optional.of(salvo);
                });
    }

    @Transactional
    public Optional<Titulo> adicionarProduto(String token, UUID tituloId, ItemProdutoDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        var tituloOpt = titulos.findByCeoIdAndId(ceoId, tituloId);
        if (tituloOpt.isEmpty()) return Optional.empty();

        var tituloAssicadoAoOperador = titulos.findFirstByEmissorIdAndPagoFalseOrderByIdAsc(emissorId);
        if(tituloAssicadoAoOperador.isEmpty()) return Optional.empty();

        if(!tituloAssicadoAoOperador.get().getId().equals(tituloOpt.get().getId()))
            return Optional.empty();

        Titulo titulo = tituloOpt.get();

        var produtoOpt = produtos.findByCeoIdAndId(ceoId, dto.getProdutoId());
        if (produtoOpt.isEmpty()) throw new IllegalStateException("Produto não encontrado");

        var produto = produtoOpt.get();

        ProdutosDosTitulos pdt = new ProdutosDosTitulos();
        pdt.setNome(produto.getNome());
        pdt.setValor(produto.getPreco());
        pdt.setQuantidade(dto.getQuantidade());
        pdt.setProduto(produto);
        pdt.setTitulo(titulo);

        titulo.getProdutosDosTitulos().add(pdt);

        // Recalcular o valor total do título
        BigDecimal total = titulo.getProdutosDosTitulos().stream()
                .map(p -> p.getValor().multiply(BigDecimal.valueOf(p.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        titulo.setValor(total);

        // Enviar alerta se necessário (mesma lógica de antes)
        if (total.compareTo(new BigDecimal("10000")) > 0) {
            var gestores = funcionarios.findAllByCeoId(ceoId).stream()
                    .filter(f -> f.getTipo().equals(TipoEspecializacao.GESTOR))
                    .toList();

            for (var f : gestores) {
                servicoEmail.enviarEmail(
                        f.getEmail(),
                        "Alerta: título elevado",
                        "O título " + titulo.getId() + " agora possui valor total de " + total + " R$."
                );
            }
        }

        if (total.compareTo(new BigDecimal("50000")) >= 0 && !titulo.isAprovado()) {
            aprovar(titulo, token);
        }

        return Optional.of(titulos.save(titulo));
    }


    // ---------- Criar Título Fornecedor ----------
    @Transactional
    public Optional<Titulo> criarTituloFornecedor(String token, UUID fornecedorId) {
        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID emissorId = jwtUtil.extrairFuncionarioId(token);

        return funcionarios.findByCeoIdAndId(ceoId, emissorId)
                .flatMap(emissor -> {
                    var fornecedorOpt = fornecedores.findByCeoIdAndId(ceoId, fornecedorId);
                    if (fornecedorOpt.isEmpty()) return Optional.empty();

                    var fornecedor = fornecedorOpt.get();

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Titulo titulo = new Titulo();
                    titulo.setCeo(ceo);
                    titulo.setFornecedor(fornecedor);
                    titulo.setCpf(fornecedor.getCpf());
                    titulo.setCnpj(fornecedor.getCnpj());
                    titulo.setTelefone(fornecedor.getTelefone());
                    titulo.setValor(BigDecimal.ZERO);
                    titulo.setProdutosDosTitulos(new HashSet<>());
                    titulo.setEmissor(emissor);

                    Titulo salvo = titulos.save(titulo);

                    servicoLogAuditoria.registrar(
                            ceo,
                            emissor,
                            "EMISSÃO",
                            "TÍTULO",
                            salvo.getId(),
                            "TÍTULO A PAGAR FORNECEDOR"
                    );

                    return Optional.of(salvo);
                });
    }

    @Transactional
    public Optional<Titulo> adicionarItemFornecedor(String token, UUID tituloId, ItemProdutoDTO dto) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        var tituloOpt = titulos.findByCeoIdAndId(ceoId, tituloId);
        if (tituloOpt.isEmpty()) return Optional.empty();

        Titulo titulo = tituloOpt.get();

        // Título precisa ser de fornecedor
        if (titulo.getFornecedor() == null) {
            throw new IllegalStateException("Este título não pertence a um fornecedor");
        }

        var produtoOpt = produtos.findByCeoIdAndId(ceoId, dto.getProdutoId());
        if (produtoOpt.isEmpty()) throw new IllegalStateException("Produto não encontrado");

        var produto = produtoOpt.get();

        // Criar item
        ProdutosDosTitulos pdt = new ProdutosDosTitulos();
        pdt.setNome(produto.getNome());
        pdt.setValor(produto.getPreco());
        pdt.setQuantidade(dto.getQuantidade());
        pdt.setProduto(produto);

        titulo.getProdutosDosTitulos().add(pdt);

        // Recalcular total negativo
        BigDecimal total = titulo.getProdutosDosTitulos().stream()
                .map(i -> i.getValor().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        titulo.setValor(total.negate()); // fornecedor sempre negativo

        Titulo salvo = titulos.save(titulo);

        return Optional.of(salvo);
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
            Boolean temFornOrFunc,
            int pagina,
            int tamanho
    ) {
        UUID ceoId = jwtUtil.extrairCeoId(token);

        Pageable pageable = PageRequest.of(pagina, tamanho);
        Specification<Titulo> spec = TituloSpecifications.comFiltros(ceoId, cpf, cnpj, nome, telefone, inicio, fim, pago, recebido, aprovado, null, temFornOrFunc);
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
        Specification<Titulo> spec = TituloSpecifications.comFiltros(ceoId, cpf, cnpj, nome, null, inicio, fim, pago, recebido, aprovado, emissorId, null);
        return PaginaDTO.from(titulos.findAll(spec, pageable));
    }

    // ---------- Remover Título ----------
    @Transactional
    public Optional<Titulo> removerTituloToken(String token, UUID tituloId, String tokenSeguranca) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        Optional<Funcionario> autorizador = funcionarios.findByCeoIdAndTokenAutorizacao(ceoId, tokenSeguranca);

        if (autorizador.isEmpty()) {
            throw new IllegalStateException("Token de autorização inválido.");
        }

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .map(titulo -> {

                    if (!titulo.isPago() && titulo.getFuncionario() == null) {
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

                    titulos.delete(titulo);

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);

                    servicoLogAuditoria.registrar(
                            ceo,
                            emissor,
                            "CANCELAR",
                            "TÍTULO no valor de " + titulo.getValor().toString(),
                            titulo.getId(),
                            "TÍTULO REMOVIDO DO SISTEMA (Autorizado por: " + autorizador.get().getNome() + ")"
                    );

                    return titulo;
                });
    }

    @Transactional
    public Optional<Titulo> removerTitulo(String token, UUID tituloId) {

        UUID ceoId = jwtUtil.extrairCeoId(token);
        UUID funcionarioId = jwtUtil.extrairFuncionarioId(token);

        return titulos.findByCeoIdAndId(ceoId, tituloId)
                .map(titulo -> {

                    if (!titulo.isPago() && titulo.getFuncionario() == null) {
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

                    titulos.delete(titulo);

                    Ceo ceo = new Ceo();
                    ceo.setId(ceoId);

                    Funcionario emissor = new Funcionario();
                    emissor.setId(funcionarioId);

                    servicoLogAuditoria.registrar(
                            ceo,
                            emissor,
                            "CANCELAR",
                            "TÍTULO no valor de " + titulo.getValor().toString(),
                            titulo.getId(),
                            "TÍTULO REMOVIDO DO SISTEMA (Autorizado por: " + emissor.getNome() + ")"
                    );

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
}
