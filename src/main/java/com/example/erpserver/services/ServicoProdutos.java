package com.example.erpserver.services;

import com.example.erpserver.models.ID;
import com.example.erpserver.models.Pessoa;
import com.example.erpserver.models.Produto;
import com.example.erpserver.models.ProdutoDTO;
import com.example.erpserver.repository.Repositorio;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Service
@Getter
public class ServicoProdutos {

    private static final Logger logger = LoggerFactory.getLogger(ServicoProdutos.class);

    private final List<Produto> produtos;
    private final Repositorio repositorio;

    public ServicoProdutos(Repositorio repositorio) {
        this.produtos = new CopyOnWriteArrayList<>(repositorio.carregarProdutos());
        this.repositorio = repositorio;
    }

    // ---------- Persistência ----------
    public void salvarJson() {
        repositorio.salvarProdutos(produtos);
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
