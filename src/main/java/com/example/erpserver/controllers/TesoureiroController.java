package com.example.erpserver.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.DTOs.ProdutoDTO;
import com.example.erpserver.entities.Fornecedor;
import com.example.erpserver.entities.Funcionario;
import com.example.erpserver.entities.Produto;
import com.example.erpserver.entities.TipoEspecializacao;
import com.example.erpserver.entities.Titulo;
import com.example.erpserver.services.ServicoFornecedores;
import com.example.erpserver.services.ServicoFuncionarios;
import com.example.erpserver.services.ServicoProdutos;
import com.example.erpserver.services.ServicoTitulos;

import jakarta.validation.Valid;

/*
 * O Tesoureiro deve:
 *       Pagar e consultar títulos para Funcionarios e Fornecedores 
 *       Consultar dados de Funcionarios e Fornecedores
 *       Consultar e Editar produtos
 * */

@RestController
@RequestMapping("/api/tesoureiro")
@Validated
public class TesoureiroController {

    private final ServicoTitulos servicoTitulos;
    private final ServicoFuncionarios servicoFuncionarios;
    private final ServicoProdutos servicoProdutos;
    private final ServicoFornecedores servicoFornecedores;

    public TesoureiroController(
        ServicoTitulos servicoTitulos,
        ServicoFuncionarios servicoFuncionarios,
        ServicoProdutos servicoProdutos,
        ServicoFornecedores servicoFornecedores
    ){
        this.servicoFornecedores = servicoFornecedores;
        this.servicoFuncionarios = servicoFuncionarios;
        this.servicoProdutos = servicoProdutos;
        this.servicoTitulos = servicoTitulos;    
    }

    // TITULOS ::
    @GetMapping("/titulos")
    public PaginaDTO<Titulo> buscarTitulos(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false)LocalDateTime inicio,
            @RequestParam(required = false)LocalDateTime fim,
            @RequestParam(required = false) Boolean pago,
            @RequestParam(required = false) Boolean recebido,
            @RequestParam(required = false) Boolean aprovado,
            @RequestParam(required = false) Boolean pagarOuReceber,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.buscarTitulos(token, cpf, cnpj, nome, telefone, inicio, fim, pago, recebido, aprovado, pagarOuReceber, pagina, tamanho);
    }

    @PutMapping("/titulos/{id}")
    public ResponseEntity<Titulo> pagarTitulo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.pagarTitulo(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
    
    // FUNCIONARIOS ::
    @GetMapping("/funcionarios")
    public PaginaDTO<Funcionario> buscarFuncionarios(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) TipoEspecializacao tipo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFuncionarios.buscarFuncionarios(token, cpf, nome, telefone, tipo, pagina, tamanho);
    }

    // FORNECEDORES ::
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

    // PRODUTOS ::
    @GetMapping("/produtos")
    public PaginaDTO<Produto> buscarProdutos(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean semEstoque,
            @RequestParam(required = false) Boolean comEstoquePendente,
            @RequestParam(required = false) Boolean comEstoqueReservado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho

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

}
