package com.example.erpserver.controllers;

import com.example.erpserver.DTOs.CadastroDTO;
import com.example.erpserver.entities.Ceo;
import com.example.erpserver.services.ServicoCadastro;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cadastro")
@Validated
public class CadastroController {

    private final ServicoCadastro servico;

    public CadastroController(ServicoCadastro servico){
        this.servico = servico;
    }

    //------- POST --------
    @PostMapping
    public ResponseEntity<Ceo> addAssinante(@RequestBody @Valid CadastroDTO dto) {
        return servico.addCeo(dto)
                .map(ceo -> ResponseEntity.status(HttpStatus.CREATED).body(ceo))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }
}
