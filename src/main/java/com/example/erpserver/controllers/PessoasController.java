package com.example.erpserver.controllers;

import com.example.erpserver.models.Pessoa;
import com.example.erpserver.models.PessoaDTO;
import com.example.erpserver.services.ServicoPessoas;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pessoas")
@Validated
public class PessoasController {

    @Autowired
    private ServicoPessoas service;

    // ------ END POINT ::::::::: PESSOAS --------
    @GetMapping
    public List<Pessoa> getPessoas(){
        return service.getPessoas();
    }

    @GetMapping("/clientes")
    public List<Pessoa> getClientes(){
        return service.getClientes();
    }

    @GetMapping("/funcionarios")
    public List<Pessoa> getFuncionarios(){
        return service.getFuncionarios();
    }

    @GetMapping("/fornecedores")
    public List<Pessoa> getFornecedores(){
        return service.getFornecedores();
    }

    @GetMapping("/{id}")
    public Optional<Pessoa> getPessoa(@PathVariable String id){
        return service.getPessoaById(id);
    }
    //------- POST --------

    @PostMapping
    public List<Pessoa> addPessoas(@RequestBody @Valid List<PessoaDTO> lista) {
        return lista.stream()
                .filter(dto -> service.getPessoaById(dto.getId()).isEmpty())
                .map(dto -> service.addPessoa(dto))
                .toList();
    }

    //------- REMOVE --------

    @DeleteMapping("/{id}")
    public Optional<Pessoa> deletePessoa(@PathVariable String id){
        return service.removePessoa(id);
    }

}
