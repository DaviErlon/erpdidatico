package com.example.erpserver.controllers;

import com.example.erpserver.DTOs.ClienteDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.DTOs.TituloDTO;
import com.example.erpserver.entities.Cliente;
import com.example.erpserver.entities.Produto;
import com.example.erpserver.entities.Titulo;
import com.example.erpserver.services.ServicoClientes;
import com.example.erpserver.services.ServicoProdutos;
import com.example.erpserver.services.ServicoTitulos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/*
* Operador de caixa (Sistema de PDV), deve:
*       Emitir títulos de Venda de produtos
*       Consultar títulos emitidos
*       Consultar dados de produtos
*       Consultar e cadastrar clientes
* */

@RestController
@RequestMapping("/api/operador")
@Validated
public class OperadorController {

    private final ServicoClientes servicoClientes;
    private final ServicoTitulos servicoTitulos;
    private final ServicoProdutos servicoProdutos;

    public OperadorController(
            ServicoClientes servicoClientes,
            ServicoTitulos servicoTitulos,
            ServicoProdutos servicoProdutos
    ){
        this.servicoClientes = servicoClientes;
        this.servicoProdutos = servicoProdutos;
        this.servicoTitulos = servicoTitulos;
    }

    @PostMapping("/vender")
    public ResponseEntity<Titulo> venderParaCliente(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TituloDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.addTituloCliente(token, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/vender/{id}")
    public ResponseEntity<Titulo> confirmarVenda(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.pagarTitulo(token, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> cadastrarCliente(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ClienteDTO dto
    ) {
        String token = authHeader.replace("Bearer ", "");

        return servicoClientes.adicionarCliente(token, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/vendas")
    public PaginaDTO<Titulo> buscarTituloEmitido(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fim,
            @RequestParam(required = false) Boolean pago,
            @RequestParam(required = false) Boolean recebido,
            @RequestParam(required = false) Boolean aprovado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.buscarTitulosEmitidos(token, cpf, null, nome, inicio, fim, pago, recebido, aprovado, pagina, tamanho);
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

    @GetMapping("/clientes")
    public PaginaDTO<Cliente> buscarCliente(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String telefone,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoClientes.buscarClientes(token, cpf, nome, telefone, pagina, tamanho);
    }

}
