package com.example.erpserver.controllers;

import com.example.erpserver.DTOs.CadastroMembroDTO;
import com.example.erpserver.entities.Membro;
import com.example.erpserver.services.ServicoMembros;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/membros")
@Validated
public class MembrosController {

    private final ServicoMembros servico;

    public MembrosController(ServicoMembros servico){
        this.servico = servico;
    }

    // ------ END POINT : GET --------

    @GetMapping("/{nome}")
    public Page<Membro> buscarMembros(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String nome,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ){
        String token = authHeader.replace("Bearer ", "");

        return servico.buscarPorNome(token, nome, pagina, tamanho);
    }

    //------- POST --------
    @PostMapping
    public ResponseEntity<Membro> adicionarMembros(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid CadastroMembroDTO dto
    ){
        String token = authHeader.replace("Bearer ", "");

        return servico.addMembro(token, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    //------- DELETE --------
    @DeleteMapping("/{id}")
    public ResponseEntity<Membro> removerMembro(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam Long id
    ){
        String token = authHeader.replace("Bearer ", "");

        return servico.removerMembro(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

}
