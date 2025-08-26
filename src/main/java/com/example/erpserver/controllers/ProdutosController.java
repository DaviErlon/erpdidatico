package com.example.erpserver.controllers;

import com.example.erpserver.models.*;
import com.example.erpserver.services.ServicoRepositorio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
@Validated
public class ProdutosController {

    @Autowired
    private ServicoRepositorio service;

    // ------ END POINT ::::::::: PRODUTOS --------
        //------- GET --------

    @GetMapping
    public List<Produto> getProdutos(){
        return service.getProdutos();
    }

    @GetMapping("/esgotado")
    public List<Produto> getForaDeEstoque(){
        return service.getProdutosForaDeEstoque();
    }

    @GetMapping("/{id}")
    public Optional<Produto> getProduto(@PathVariable String id){
        return service.getProdutoById(id);
    }

        //------- POST --------

    @PostMapping("/produtos")
    public List<Produto> addProdutos(@RequestBody @Valid List<ProdutoDTO> lista) {
        return lista.stream().map(dto -> service.addProduto(dto)).toList();
    }

        //------- REMOVE --------

    @DeleteMapping("/produtos/{id}")
    public Optional<Produto> deleteProduto(@PathVariable String id){
        return service.removeProduto(id);
    }
}
