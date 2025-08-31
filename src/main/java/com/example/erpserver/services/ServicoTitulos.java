package com.example.erpserver.services;

import com.example.erpserver.models.ID;
import com.example.erpserver.models.Titulo;
import com.example.erpserver.models.TituloDTO;
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
public class ServicoTitulos {

    private static final Logger logger = LoggerFactory.getLogger(ServicoTitulos.class);

    private final List<Titulo> titulos;
    private final Repositorio repositorio;

    public ServicoTitulos(Repositorio repositorio) {
        this.titulos = new CopyOnWriteArrayList<>(repositorio.carregarTitulos());
        this.repositorio = repositorio;
    }

    // ---------- Persistência ----------
    public void salvarJson() {
        repositorio.salvarTitulos(titulos);
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
