package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "funcionarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @JsonIgnore
    @Id
    @Column(length = 11)
    private String cpf;

    private String nome;

    @JsonIgnore
    private double salario;

    @JsonIgnore
    private String contato;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "assinante_id", nullable = false)
    private Assinante assinante;
}