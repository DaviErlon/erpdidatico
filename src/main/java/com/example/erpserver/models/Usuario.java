package com.example.erpserver.models;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Usuario {
    private String username;
    private String senha;
    private String role; // "ADMIN" ou "USER"
}
