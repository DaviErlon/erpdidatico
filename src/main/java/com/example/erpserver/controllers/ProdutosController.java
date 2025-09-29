package com.example.erpserver.controllers;

import com.example.erpserver.DTOs.ProdutoDTO;
import com.example.erpserver.entities.Produto;
import com.example.erpserver.services.ServicoProdutos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@Validated
public class ProdutosController {

    private final ServicoProdutos servico;

    public ProdutosController(ServicoProdutos servico) {
        this.servico = servico;
    }

    // ------ END POINT : GET --------
    @GetMapping
    public Page<Produto> buscarProduto(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) Boolean semEstoque,
        @RequestParam(required = false) Boolean comEstoquePendente,
        @RequestParam(required = false) Boolean comEstoqueReservado,
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanho
    ){
        String token = authHeader.replace("Bearer ", "");

        return servico.buscarProdutos(token, nome, semEstoque, comEstoquePendente, comEstoqueReservado, pagina, tamanho);
    }

    // ------ END POINT : POST --------
    @PostMapping
    public ResponseEntity<Produto> postProduto(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProdutoDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.addProduto(dto, token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // ------ END POINT : PUT --------
    @PutMapping("/{id}")
    public ResponseEntity<Produto> alterarProduto(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProdutoDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.atualizarPorId(token, id, dto.getNome(), dto.getPreco(), dto.getQuantidade())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // ------ END POINT : DELETE --------
    @DeleteMapping("/{id}")
    public ResponseEntity<Produto> deletarProduto(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servico.removerPorId(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

}
