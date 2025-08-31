package com.example.erpserver.services;

import com.example.erpserver.models.Pessoa;
import com.example.erpserver.models.PessoaDTO;
import com.example.erpserver.repository.Repositorio;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Getter
public class ServicoPessoas {

    private static final Logger logger = LoggerFactory.getLogger(ServicoPessoas.class);

    private final List<Pessoa> pessoas;
    private final Repositorio repositorio;

    public ServicoPessoas(Repositorio repositorio) {
        this.pessoas = new CopyOnWriteArrayList<>(repositorio.carregarPessoas());
        this.repositorio = repositorio;
    }

    // ---------- Persistência ----------
    public void salvarJson() {
        repositorio.salvarPessoas(pessoas);
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
}
