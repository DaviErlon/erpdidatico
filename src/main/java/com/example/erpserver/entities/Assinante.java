package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assinantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assinante {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(unique = true)
    private String cpf;

    private String nome;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String senhaHash;

    private int plano;

    @JsonIgnore
    private LocalDateTime criadoEm = LocalDateTime.now();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Funcionario> funcionarios = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pessoa> pessoas = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> produtos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Titulo> titulos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membro> membros = new ArrayList<>();

}
