package com.example.erpserver.controllers;

import com.example.erpserver.models.Titulo;
import com.example.erpserver.models.TituloDTO;
import com.example.erpserver.services.ServicoTitulos;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/titulos")
@Validated
public class TitulosController {

    @Autowired
    private ServicoTitulos service;

    // ------ END POINT ::::::::: TITULOS --------
    // ------- GET --------

    @GetMapping
    public List<Titulo> getTitulos(){
        return service.getTitulos();
    }

    @GetMapping("/pagos")
    public List<Titulo> getPagos(){
        return service.getTitulosPagos();
    }

    @GetMapping("/abertos")
    public List<Titulo> getAbertos(){
        return service.getTitulosEmAberto();
    }

    @GetMapping("/{id}")
    public Optional<Titulo> getById(@PathVariable String id){
        return service.getTituloById(id);
    }

    //------- POST --------

    @PostMapping
    public List<Titulo> addTitulos(@RequestBody @Valid List<TituloDTO> lista){
        return lista.stream().map(dto -> service.addTitulo(dto)).toList();
    }

    //------- REMOVE --------

    @DeleteMapping("/{id}")
    public Optional<Titulo> deleteTitulo(@PathVariable String id){
        return service.removeTitulo(id);
    }

    //------- PUT --------
    @PutMapping("/{id}")
    public Optional<Titulo> pagarTitulo(@Valid @PathVariable String id){
        return service.efetuarPagamento(id);
    }
}
