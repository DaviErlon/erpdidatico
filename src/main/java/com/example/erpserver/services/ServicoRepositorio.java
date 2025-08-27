package com.example.erpserver.services;

import com.example.erpserver.models.*;
import com.example.erpserver.repository.Repositorio;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Getter
@Service
public class ServicoRepositorio {

    private static final Logger logger = LoggerFactory.getLogger(ServicoRepositorio.class);

    private final List<Pessoa> pessoas;
    private final List<Produto> produtos;
    private final List<Titulo> titulos;

    private final Repositorio repositorio;

    public ServicoRepositorio(Repositorio repositorio) {
        this.repositorio = repositorio;
        // thread-safe
        this.pessoas = new CopyOnWriteArrayList<>(repositorio.carregarPessoas());
        this.produtos = new CopyOnWriteArrayList<>(repositorio.carregarProdutos());
        this.titulos = new CopyOnWriteArrayList<>(repositorio.carregarTitulos());
    }

    // ---------- Persistência ----------
    public void salvarJson() {
        repositorio.salvarTitulos(titulos);
        repositorio.salvarProdutos(produtos);
        repositorio.salvarPessoas(pessoas);
        logger.info("Salvamento automático concluído.");
    }

    // ---------- Títulos ----------
    public Optional<Titulo> efetuarPagamento(String tituloId) {
        Optional<Titulo> t = getTituloById(tituloId);
        t.ifPresent(titulo -> titulo.setPago(true));
        return t;
    }

    public Titulo addTitulo(TituloDTO t) {
        String id = generateId(titulos);
        Titulo titulo = new Titulo(id, t.getValor(), t.getCpf(), false, t.isPagaroureceber());
        titulos.add(titulo);
        logger.info("Título adicionado com sucesso: {}", titulo);
        return titulo;
    }

    public List<Titulo> getTitulosPagos() {
        return titulos.stream().filter(Titulo::isPago).collect(Collectors.toList());
    }

    public List<Titulo> getTitulosEmAberto() {
        return titulos.stream().filter(t -> !t.isPago()).collect(Collectors.toList());
    }

    public Optional<Titulo> getTituloById(String id) {
        return titulos.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Optional<Titulo> removeTitulo(String id) {
        Optional<Titulo> t = getTituloById(id);
        t.ifPresent(titulos::remove);
        if (t.isEmpty()) {
            logger.warn("Não existe título com esse ID: {}", id);
        } else {
            logger.info("Título removido com sucesso: {}", t.get());
        }
        return t;
    }

    // ---------- Pessoas ----------
    public Pessoa addPessoa(PessoaDTO p) {
        Pessoa pessoa = new Pessoa(
                p.getId(),
                p.getTipo(),
                p.getNome()
            );
        pessoas.add(pessoa);
        logger.info("Pessoa adicionada com sucesso: {}", pessoa);
        return pessoa;
    }

    public List<Pessoa> getClientes() {
        return pessoas.stream().filter(p -> p.getTipo() == 0).collect(Collectors.toList());
    }

    public List<Pessoa> getFuncionarios() {
        return pessoas.stream().filter(p -> p.getTipo() == 1).collect(Collectors.toList());
    }

    public List<Pessoa> getFornecedores() {
        return pessoas.stream().filter(p -> p.getTipo() == 2).collect(Collectors.toList());
    }

    public Optional<Pessoa> getPessoaById(String id) {
        return pessoas.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Optional<Pessoa> removePessoa(String id) {
        Optional<Pessoa> p = getPessoaById(id);
        p.ifPresent(pessoas::remove);
        if (p.isEmpty()) {
            logger.warn("Não existe pessoa com esse ID: {}", id);
        } else {
            logger.info("Pessoa removida com sucesso: {}", p.get());
        }
        return p;
    }

    // ---------- Produtos ----------
    public Produto addProduto(ProdutoDTO p) {
        Produto produto = new Produto(
                generateId(produtos),
                p.getNome(),
                p.getPreco(),
                p.getEstoque()
            );
        produtos.add(produto);
        logger.info("Produto adicionado com sucesso: {}", produto);
        return produto;
    }

    public List<Produto> getProdutosForaDeEstoque() {
        return produtos.stream().filter(pr -> pr.getEstoque() == 0).collect(Collectors.toList());
    }

    public Optional<Produto> getProdutoById(String id) {
        return produtos.stream().filter(pr -> pr.getId().equals(id)).findFirst();
    }

    public Optional<Produto> removeProduto(String id) {
        Optional<Produto> pr = getProdutoById(id);
        pr.ifPresent(produtos::remove);
        if (pr.isEmpty()) {
            logger.warn("Não existe produto com esse ID: {}", id);
        } else {
            logger.info("Produto removido com sucesso: {}", pr.get());
        }
        return pr;
    }

    // ---------- Auxiliares ----------
    private <T extends ID> String generateId(List<T> models) {
        Set<String> existingIds = models.stream().map(ID::getId).collect(Collectors.toSet());
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (existingIds.contains(id));
        return id;
    }
}

