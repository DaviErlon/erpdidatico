package com.example.erpserver.controllers;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
}
