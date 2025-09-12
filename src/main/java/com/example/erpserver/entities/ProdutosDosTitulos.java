package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "produtos_em_titulos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutosDosTitulos {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantidadeProduto;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "titulo_id", nullable = false)
    private Titulo titulo;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

}
