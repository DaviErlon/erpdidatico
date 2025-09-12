package com.example.erpserver.controllers;

import com.example.erpserver.entities.Pessoa;
import com.example.erpserver.DTOs.PessoaDTO;
import com.example.erpserver.services.ServicoPessoas;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pessoas")
@Validated
public class PessoasController {

    private ServicoPessoas servico;

    public PessoasController(ServicoPessoas servico) {
        this.servico = servico;
    }

    // ------ END POINT : GET --------
    @GetMapping
    public Page<Pessoa> buscarPessoas(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean fornecedor,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ) {
        String token = authHeader.replace("Bearer ", "");
        return servico.buscarPessoas(token, cpf, nome, fornecedor, pagina, tamanho);
    }

    // ------ END POINT : POST --------

    @PostMapping
    public ResponseEntity<Pessoa> postPessoa(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PessoaDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.addPessoa(dto, token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // ------ END POINT : PUT --------

    @PutMapping("/{id}")
    public ResponseEntity<Pessoa> alterarPessoa(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PessoaDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.atualizarPessoa(token, id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // ------ END POINT : REMOVE --------

    @DeleteMapping("/{id}")
    public ResponseEntity<Pessoa> deletarPessoa(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.removerPorId(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
