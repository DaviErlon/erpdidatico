package com.example.erpserver.controllers;

import com.example.erpserver.models.Titulo;
import com.example.erpserver.models.TituloDTO;
import com.example.erpserver.services.ServicoRepositorio;
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
    private ServicoRepositorio service;

    // ------ END POINT ::::::::: TITULOS --------
    // ------- GET --------

    @GetMapping("/titulos")
    public List<Titulo> getTitulos(){
        return service.getTitulos();
    }

    @GetMapping("/titulos/pagos")
    public List<Titulo> getPagos(){
        return service.getTitulosPagos();
    }

    @GetMapping("/titulos/abertos")
    public List<Titulo> getAbertos(){
        return service.getTitulosEmAberto();
    }

    @GetMapping("/titulos/{id}")
    public Optional<Titulo> getById(@PathVariable String id){
        return service.getTituloById(id);
    }

    //------- POST --------

    @PostMapping("/titulos")
    public List<Titulo> addTitulos(@RequestBody @Valid List<TituloDTO> lista){
        return lista.stream().map(dto -> service.addTitulo(dto)).toList();
    }

    //------- REMOVE --------

    @DeleteMapping("/titulos/{id}")
    public Optional<Titulo> deleteTitulo(@PathVariable String id){
        return service.removeTitulo(id);
    }
}
