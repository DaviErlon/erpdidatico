package com.example.erpserver.controllers;

import com.example.erpserver.DTOs.CadastroFuncionarioDTO;
import com.example.erpserver.DTOs.FuncionarioDTO;
import com.example.erpserver.DTOs.PaginaDTO;
import com.example.erpserver.entities.*;
import com.example.erpserver.services.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * O Gestor/gerete deve:
 *       Aprovar títulos altos
 *       Cancelar/Excluir títulos
 *       CRUD de funcionarios
 *       Consultar dados
 *       Monitorar operações internas
 * */


@RestController
@RequestMapping("/api/gestor")
@Validated
public class GestorController {

    private final ServicoTitulos servicoTitulos;
    private final ServicoLogAuditoria servicoLogAuditoria;
    private final ServicoFuncionarios servicoFuncionarios;
    private final ServicoProdutos servicoProdutos;
    private final ServicoClientes servicoClientes;
    private final ServicoFornecedores servicoFornecedores;

    public GestorController(
            ServicoTitulos servicoTitulos,
            ServicoLogAuditoria servicoLogAuditoria,
            ServicoFuncionarios servicoFuncionarios,
            ServicoProdutos servicoProdutos,
            ServicoClientes servicoClientes,
            ServicoFornecedores servicoFornecedores
    ){
        this.servicoClientes = servicoClientes;
        this.servicoFornecedores = servicoFornecedores;
        this.servicoFuncionarios = servicoFuncionarios;
        this.servicoTitulos = servicoTitulos;
        this.servicoProdutos = servicoProdutos;
        this.servicoLogAuditoria = servicoLogAuditoria;
    }

    @PutMapping("/titulos")
    public ResponseEntity<Titulo> aprovarTitulo(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam UUID tituloId
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.aprovarTitulo(tituloId, token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/titulos")
    public ResponseEntity<Titulo> removerTitulo(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam UUID tituloId
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoTitulos.removerTitulo(token, tituloId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<Funcionario> adicionarFuncionario(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid FuncionarioDTO dto
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFuncionarios.adicionarFuncionario(token, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/funcionarios/promover")
    public ResponseEntity<Funcionario> promoverFuncionario(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam UUID funcionarioId,
            @RequestBody @Valid CadastroFuncionarioDTO dto
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFuncionarios.promoverFuncionario(token, funcionarioId ,dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/funcionarios")
    public ResponseEntity<Funcionario> editarFuncionario(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam UUID funcionarioId,
            @RequestBody @Valid FuncionarioDTO dto
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFuncionarios.editarFuncionario(token, funcionarioId ,dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/funcionarios")
    public ResponseEntity<Funcionario> removerFuncionario(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam UUID funcionarioId
    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFuncionarios.removerFuncionario(token, funcionarioId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/funcionarios")
    public PaginaDTO<Funcionario> buscarFuncionarios(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) TipoEspecializacao tipo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoFuncionarios.buscarFuncionarios(token, cpf, nome, telefone, tipo, pagina, tamanho);
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
    public PaginaDTO<Cliente> buscarClientes(
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

    @GetMapping("/logs")
    public PaginaDTO<LogAuditoria> buscarLogs(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) UUID emissorId,
            @RequestParam(required = false)LocalDateTime inicio,
            @RequestParam(required = false)LocalDateTime fim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho

    ){
        String token = authHeader.replace("Bearer ", "");

        return servicoLogAuditoria.buscarLogs(token, emissorId, inicio, fim, pagina, tamanho);
    }

}
