package com.example.erpserver.controllers;

import com.example.erpserver.models.Token;
import com.example.erpserver.models.Usuario;
import com.example.erpserver.services.Autenticador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:55906")
@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private Autenticador autenticador;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {

        Optional<String> token = autenticador.logarUsuario(loginRequest);
        if (token.isPresent()) {
            return ResponseEntity.ok(new Token(token.get()));
        } else {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }

}
