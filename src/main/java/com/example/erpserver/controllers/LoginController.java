package com.example.erpserver.controllers;

import com.example.erpserver.DTOs.LoginDTO;
import com.example.erpserver.DTOs.TokenDTO;
import com.example.erpserver.services.ServicoLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/login")
@Validated
public class LoginController {

    private final ServicoLogin servicoLogin;

    public LoginController(ServicoLogin servicoLogin) {
        this.servicoLogin = servicoLogin;
    }

    @PostMapping
    public ResponseEntity<?> autenticar(@RequestBody LoginDTO loginRequest) {
        Optional<String> tokenOpt = servicoLogin.autenticar(
                loginRequest.getEmail(), loginRequest.getSenha()
        );

        if (tokenOpt.isPresent()) {
            TokenDTO token = new TokenDTO(tokenOpt.get());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas");
        }
    }

}

