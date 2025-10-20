package com.example.erpserver.controllers;

import com.example.erpserver.entities.Titulo;
import com.example.erpserver.DTOs.TituloDTO;
import com.example.erpserver.services.ServicoTitulos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/titulos")
@Validated
public class TitulosController {

    private final ServicoTitulos servico;

    public TitulosController(ServicoTitulos servico){
        this.servico = servico;
    }
    // ------ END POINT : GET --------
    @GetMapping
    public Page<Titulo> buscarTitulo(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fim,
            @RequestParam(required = false) Boolean pago,
            @RequestParam(required = false) Boolean aReceber,
            @RequestParam(required = false) Boolean aPagar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servico.buscarTitulos(token, cpf, nome, inicio, fim, pago, aReceber, aPagar, pagina, tamanho);

    }
    //------- POST --------
    @PostMapping
    public ResponseEntity<Titulo> postTitulo(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TituloDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.addTitulo(token, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }


    //------- REMOVE --------
    @DeleteMapping("/{id}")
    public ResponseEntity<Titulo> deletarTitulo(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        return servico.removerTitulo(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    //------- PUT --------
    @PutMapping("/{id}")
    public ResponseEntity<Titulo> alterarTitulo(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TituloDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.editarTitulo(token, id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/quitar/{id}")
    public ResponseEntity<Titulo> quitarTitulo(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.quitarTitulo(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
