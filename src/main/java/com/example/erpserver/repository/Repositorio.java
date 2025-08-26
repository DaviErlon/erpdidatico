package com.example.erpserver.repository;

import com.example.erpserver.models.Pessoa;
import com.example.erpserver.models.Produto;
import com.example.erpserver.models.Titulo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class Repositorio {

    private static final String PRODUTOS_ARQUIVO = "data/produtos.json";
    private static final String TITULOS_ARQUIVO = "data/titulos.json";
    private static final String PESSOAS_ARQUIVO = "data/pessoas.json";

    private final ObjectMapper mapper = new ObjectMapper();

    // -------------------- PESSOAS --------------------
    public List<Pessoa> carregarPessoas() {
        return lerLista(PESSOAS_ARQUIVO, new TypeReference<List<Pessoa>>() {});
    }

    public synchronized void salvarPessoas(List<Pessoa> pessoas) {
        salvarLista(PESSOAS_ARQUIVO, pessoas);
    }
    // -------------------- TITULOS --------------------
    public List<Titulo> carregarTitulos() {
        return lerLista(TITULOS_ARQUIVO, new TypeReference<List<Titulo>>() {});
    }

    public synchronized void salvarTitulos(List<Titulo> titulos) {
        salvarLista(TITULOS_ARQUIVO, titulos);
    }

    // -------------------- PRODUTOS --------------------
    public List<Produto> carregarProdutos() {
        return lerLista(PRODUTOS_ARQUIVO, new TypeReference<List<Produto>>() {});
    }

    public synchronized void salvarProdutos(List<Produto> produtos) {
        salvarLista(PRODUTOS_ARQUIVO, produtos);
    }

    // -------------------- MÉTODOS AUXILIARES --------------------
    private <T> List<T> lerLista(String arquivo, TypeReference<List<T>> typeRef) {
        Path path = Path.of(arquivo);
        try {
            if (Files.notExists(path)) {
                return new ArrayList<>();
            }
            return mapper.readValue(path.toFile(), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo " + arquivo, e);
        }
    }

    private <T> void salvarLista(String arquivo, List<T> lista) {
        Path path = Path.of(arquivo);
        try {
            Files.createDirectories(path.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), lista);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo " + arquivo, e);
        }
    }
}
