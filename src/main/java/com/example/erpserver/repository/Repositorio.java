package com.example.erpserver.repository;

import com.example.erpserver.models.Pessoa;
import com.example.erpserver.models.Produto;
import com.example.erpserver.models.Titulo;
import com.example.erpserver.models.Usuario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class Repositorio {

    private static final Path PRODUTOS_ARQUIVO = Path.of("data/produtos.json");
    private static final Path TITULOS_ARQUIVO  = Path.of("data/titulos.json");
    private static final Path PESSOAS_ARQUIVO  = Path.of("data/pessoas.json");
    private static final Path USUARIOS_ARQUIVO = Path.of("data/usuarios.json");

    // Leitura concorrente liberada, escrita exclusiva (Segurança de dados de forma paralela)
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // -------------------- PESSOAS --------------------
    public List<Pessoa> carregarPessoas() {
        lock.readLock().lock();
        try {
            return lerLista(PESSOAS_ARQUIVO, new TypeReference<List<Pessoa>>() {});
        } finally {
            lock.readLock().unlock();
        }
    }

    public void salvarPessoas(List<Pessoa> pessoas) {
        salvarListaComLock(PESSOAS_ARQUIVO, pessoas);
    }

    // -------------------- TITULOS --------------------
    public List<Titulo> carregarTitulos() {
        lock.readLock().lock();
        try {
            return lerLista(TITULOS_ARQUIVO, new TypeReference<List<Titulo>>() {});
        } finally {
            lock.readLock().unlock();
        }
    }

    public void salvarTitulos(List<Titulo> titulos) {
        salvarListaComLock(TITULOS_ARQUIVO, titulos);
    }

    // -------------------- PRODUTOS --------------------
    public List<Produto> carregarProdutos() {
        lock.readLock().lock();
        try {
            return lerLista(PRODUTOS_ARQUIVO, new TypeReference<List<Produto>>() {});
        } finally {
            lock.readLock().unlock();
        }
    }

    public void salvarProdutos(List<Produto> produtos) {
        salvarListaComLock(PRODUTOS_ARQUIVO, produtos);
    }

    // -------------------- USUARIOS --------------------
    public List<Usuario> carregarUsuarios() {
        lock.readLock().lock();
        try {
            return lerLista(USUARIOS_ARQUIVO, new TypeReference<List<Usuario>>() {});
        } finally {
            lock.readLock().unlock();
        }
    }

    public void salvarUsuarios(List<Usuario> usuarios) {
        salvarListaComLock(USUARIOS_ARQUIVO, usuarios);
    }

    // -------------------- MÉTODOS AUXILIARES --------------------
    private <T> List<T> lerLista(Path arquivo, TypeReference<List<T>> typeRef) {
        try {
            if (Files.notExists(arquivo)) {
                return new ArrayList<>();
            }
            return mapper.readValue(arquivo.toFile(), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo " + arquivo, e);
        }
    }

    private <T> void salvarListaComLock(Path arquivo, List<T> lista) {
        lock.writeLock().lock();
        try {
            salvarListaAtomica(arquivo, lista);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Escrita atômica: grava em .tmp e move substituindo o antigo se ok! (Evite corrupção dos dados). */
    private <T> void salvarListaAtomica(Path arquivo, List<T> lista) {
        try {
            Path dir = arquivo.getParent();
            if (dir != null) Files.createDirectories(dir);

            Path tmp = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");

            mapper.writeValue(tmp.toFile(), lista);

            // Move atômico quando suportado pelo SO, senão substitui.
            try {
                Files.move(tmp, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo " + arquivo, e);
        }
    }
}
