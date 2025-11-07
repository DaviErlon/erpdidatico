package com.example.erpserver.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.erpserver.DTOs.FornecedorDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.DTOs.ProdutoDTO;
import com.example.erpserver.entities.Fornecedor;
import com.example.erpserver.entities.Produto;
import com.example.erpserver.entities.Titulo;
import com.example.erpserver.services.ServicoFornecedores;
import com.example.erpserver.services.ServicoProdutos;
import com.example.erpserver.services.ServicoTitulos;

import jakarta.validation.Valid;

/*
 * O Almoxarife deve:
 *       Receber e consultar titulos de fornecedores
 *       CRUD de produtos 
 *       CRUD de fornecedores      
 * */

@RestController
@RequestMapping("/api/almoxarife")
@Validated
public class AlmoxarifeController {
    
    private final ServicoTitulos servicoTitulos;
    private final ServicoFornecedores servicoFornecedores;
    private final ServicoProdutos servicoProdutos;

    public AlmoxarifeController(
        ServicoTitulos servicoTitulos,
        ServicoFornecedores servicoFornecedores,
        ServicoProdutos servicoProdutos
    ){
        this.servicoFornecedores = servicoFornecedores;
        this.servicoProdutos = servicoProdutos;
        this.servicoTitulos = servicoTitulos;
    }

        // PRODUTOS ::
    @PostMapping("/produtos")
    public ResponseEntity<Produto> adicionarProduto(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProdutoDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servicoProdutos.adicionarProduto(token, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/produtos")
    public PaginaDTO<Produto> buscarProdutos(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean semEstoque,
            @RequestParam(required = false) Boolean comEstoquePendente,
            @RequestParam(required = false) Boolean comEstoqueReservado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoProdutos.buscarProdutos(token, nome, semEstoque, comEstoquePendente, comEstoqueReservado, pagina, tamanho);
    }

    @PutMapping("/produtos/{id}")
    public ResponseEntity<Produto> alterarProduto(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProdutoDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servicoProdutos.atualizarProduto(token, id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Produto> deletarProduto(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servicoProdutos.removerProduto(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // FORNECEDORES ::
    @PostMapping("/fornecedores")
    public ResponseEntity<Fornecedor> adicionarFornecedores(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FornecedorDTO dto
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFornecedores.adicionarFornecedor(token, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/fornecedores/{id}")
    public ResponseEntity<Fornecedor> editarFornecedores(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FornecedorDTO dto,
            @PathVariable UUID id
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFornecedores.atualizarFornecedor(token, id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/fornecedores/{id}")
    public ResponseEntity<Fornecedor> excluirFornecedores(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFornecedores.removerFornecedor(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/fornecedores")
    public PaginaDTO<Fornecedor> buscarFornecedores(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String telefone,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFornecedores.buscarFornecedores(token, cpf, cnpj, nome, telefone, pagina, tamanho);
    }

    // TITULOS ::
    @PutMapping("/titulos/{id}")
    public ResponseEntity<Titulo> receberTitulo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.movimentarEstoqueTitulo(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
    
    @GetMapping("/titulos")
    public PaginaDTO<Titulo> buscarTitulos(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fim,
            @RequestParam(required = false) Boolean pago,
            @RequestParam(required = false) Boolean recebido,
            @RequestParam(required = false) Boolean aprovado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.buscarTitulos(token, cpf, cnpj, nome, telefone, inicio, fim, pago, recebido, aprovado, true, pagina, tamanho);
    }
}
