package com.example.erpserver.controllers;

import com.example.erpserver.models.Token;
import com.example.erpserver.models.UsuarioDTO;
import com.example.erpserver.services.ServicoLogin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final ServicoLogin servicoLogin;

    public LoginController(ServicoLogin servicoLogin) {
        this.servicoLogin = servicoLogin;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody UsuarioDTO loginRequest) {
        Optional<String> tokenOpt = servicoLogin.autenticar(
                loginRequest.getUsername(), loginRequest.getSenha()
        );

        if (tokenOpt.isPresent()) {
            Token token = new Token(tokenOpt.get());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas");
        }
    }

}

