package com.example.erpserver.controllers;

import com.example.erpserver.DTOs.LoginDTO;
import com.example.erpserver.DTOs.RespostaDTO;
import com.example.erpserver.services.ServicoLogin;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
@Validated
public class LoginController {

    private final ServicoLogin servicoLogin;

    public LoginController(ServicoLogin servicoLogin) {
        this.servicoLogin = servicoLogin;
    }

    @PostMapping
    public ResponseEntity<RespostaDTO> autenticar(
            @RequestBody @Valid LoginDTO loginRequest
    ) {
        return servicoLogin.autenticar(loginRequest.getEmail(), loginRequest.getSenha())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}

